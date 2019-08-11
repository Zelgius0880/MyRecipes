package zelgius.com.myrecipes

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.repository.AppDatabase
import zelgius.com.myrecipes.repository.IngredientRepository
import zelgius.com.myrecipes.repository.RecipeRepository
import zelgius.com.myrecipes.repository.StepRepository
import zelgius.com.myrecipes.utils.PdfGenerator
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.myrecipes.worker.DownloadImageWorker
import zelgius.com.protobuff.RecipeProto
import java.io.File

val TAG = RecipeViewModel::class.simpleName

class RecipeViewModel(val app: Application) : AndroidViewModel(app) {

    /* var user: FirebaseUser? = null
         set(value) {
             field = value
             connectedUser.value = value
         }*/

    private val recipeRepository = RecipeRepository(app)
    private val ingredientRepository = IngredientRepository(app)
    private val stepRepository = StepRepository(app)

    /*val connectedUser = MutableLiveData<FirebaseUser?>()*/
    val selectedRecipe = MutableLiveData<Recipe>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri?>()

    var currentRecipe: Recipe = Recipe()

    val mealList = LivePagedListBuilder(recipeRepository.pagedMeal(), /* page size  */ 20).build()
    val dessertList =
        LivePagedListBuilder(recipeRepository.pagedDessert(), /* page size  */ 20).build()
    val otherList = LivePagedListBuilder(recipeRepository.pagedOther(), /* page size  */ 20).build()

    //val searchResult: MutableLiveData<>

    //val storageRef by lazy { FirebaseStorage.getInstance().reference }
    val ingredients: LiveData<List<Ingredient>>

    private val searchQuery = MutableLiveData<String>()
    val searchResult = Transformations.switchMap(searchQuery) {
        recipeRepository.pagedSearch(it).toLiveData(20)
    }

    init {
        ingredients = ingredientRepository.get()
    }

    /*fun uploadFile(recipe: Recipe, file: File, callback: (Boolean) -> Unit = {}) {
        val ref = storageRef.child("images/${recipe.name}.png")

        val uploadTask = ref.putFile(file.toUri())

        uploadTask.continueWithTask {
            if (!it.isSuccessful) {
                throw it.exception!!
            }

            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                recipe.imageURL = it.result!!.path
            }
            callback(it.isSuccessful)
        }
    }*/

    fun search(query: String): LiveData<PagedList<Recipe>> {
        searchQuery.value = query

        return searchResult
    }

    fun saveCurrentRecipe(): LiveData<Recipe> =
        saveRecipe(currentRecipe)

    fun saveRecipe(recipe: Recipe): LiveData<Recipe> {
        val done = MutableLiveData<Recipe>()

        runInTransaction {
            viewModelScope.launch {
                if (recipe.id == null)
                    recipe.id = recipeRepository.insert(recipe)
                else
                    recipeRepository.update(recipe)

                recipe.steps.forEach {
                    it.refRecipe = recipe.id

                    if (it.id == null)
                        it.id = stepRepository.insert(it)
                    else stepRepository.update(it)
                }

                recipe.ingredients.forEach {
                    it.refRecipe = recipe.id
                    it.refStep = it.step?.id

                    if (it.id == null)
                        it.id = ingredientRepository.insert(it, recipe)
                    else
                        ingredientRepository.update(it)
                }

                ingredientRepository.deleteAllButThem(recipe, recipe.ingredients)
                stepRepository.deleteAllButThem(recipe, recipe.steps)

                done.value = recipe

                val worker = OneTimeWorkRequestBuilder<DownloadImageWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString("URL", recipe.imageURL)
                            .putLong("ID", recipe.id ?: 0L)
                            .build()
                    )
                    .setConstraints(Constraints.NONE)
                    .build()

                WorkManager
                    .getInstance()
                    .enqueue(worker)
            }
        }

        return done
    }

    fun loadRecipe(id: Long): LiveData<Recipe?> {
        val done = MutableLiveData<Recipe?>()

        viewModelScope.launch {

            recipeRepository.getFull(id).apply {
                if (this != null)
                    selectedRecipe.value = this

                selectedImageUrl.value = this?.imageURL?.toUri()
                done.value = this
            }
        }

        return done
    }


    fun delete(recipe: Recipe): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            ingredientRepository.delete(recipe)

            stepRepository.delete(recipe)

            recipeRepository.delete(recipe)

            result.value = true

        }

        return result
    }

    fun removeImage(recipe: Recipe) {
        File(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${recipe.id}").delete()
    }

    private fun runInTransaction(runner: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                AppDatabase.getInstance(app).runInTransaction(runner)
            }
        }
    }


    fun exportToPdf(recipe: Recipe, file: File): LiveData<File> {
        val result = MutableLiveData<File>()
        viewModelScope.launch {

            result.value = PdfGenerator(app).createPdf(recipe, file)
        }

        return result
    }

    fun saveFromQrCode(scannedBase64: String): LiveData<Recipe?> {

        val result = MutableLiveData<Recipe?>()

        runInTransaction {
            viewModelScope.launch {
                result.value = try {
                    val bytes = Base64.decode(scannedBase64, Base64.NO_PADDING).unzip()
                    val proto = RecipeProto.Recipe.parseFrom(bytes)
                    val recipe = Recipe(proto)

                    recipe.ingredients.forEach {
                        it.id = ingredientRepository.get(it.name)?.id
                        it.step = recipe.steps.find { s -> s == it.step }
                    }

                    recipe.id = recipeRepository.insert(recipe)

                    recipe.steps.forEach {
                        it.refRecipe = recipe.id
                        it.id = stepRepository.insert(it)
                    }

                    recipe.ingredients.forEach {
                        it.refRecipe = recipe.id
                        it.refStep = it.step?.id

                        if (it.id == null)
                            it.id = ingredientRepository.insert(it, recipe)
                        else
                            ingredientRepository.update(it)
                    }

                    recipe
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        return result
    }

    fun buildNotification(recipe: Recipe) {
        val list = mutableListOf<Parcelable>()
        list.addAll(recipe.ingredients.filter { it.step == null }.sortedBy { it.sortOrder })
        recipe.steps.forEach { s ->
            list.addAll(recipe.ingredients.filter { it.step == s }.sortedBy { it.sortOrder })
            list.add(s)
        }


        if (list.isNotEmpty()) {
            val name = app.getString(R.string.channel_name)
            val descriptionText = app.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("recipe", name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                vibrationPattern = longArrayOf(0L)
                enableVibration(true)
            }

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.deleteNotificationChannel("recipe")
            notificationManager.createNotificationChannel(channel)

            //This is the intent of PendingIntent
            val intentAction = Intent(app, ActionBroadcastReceiver::class.java)

            //This is optional if you have more than one buttons and want to differentiate between two
            intentAction.putExtra("LIST", list.toTypedArray())
            intentAction.putExtra("INDEX", 0)
            intentAction.putExtra("TITLE", recipe.name)
            intentAction.putExtra("ID_FROM_NOTIF", recipe.id)

            val o = list.first()
            val text = when (o) {
                is Step -> o.text
                is IngredientForRecipe -> IngredientForRecipe.text(app, o)
                else -> error("Should not be there")
            }

            val drawable = when (o) {
                is Step -> UiUtils.getDrawable(
                    app,
                    "${o.order}"
                )
                is IngredientForRecipe -> UiUtils.getDrawable(
                    app, if (!o.imageUrl.isNullOrEmpty()) o.imageUrl!! else o.name
                )
                else -> error("Should not be there")
            }

            val pIntent =
                PendingIntent.getBroadcast(app, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(app, "recipe")
                .setSmallIcon(R.drawable.ic_restaurant_menu_black_24dp)
                .setContentTitle(recipe.name)
                .setContentText(text)
                .setLargeIcon(drawable!!.toBitmap())
                .setSound(null)
                .setVibrate(longArrayOf(0L))
                .addAction(
                    R.drawable.ic_skip_next_black_24dp,
                    app.getString(R.string.next),
                    pIntent
                )
                .setContentIntent(
                    PendingIntent.getActivity(app, 0,
                        Intent(app, MainActivity::class.java ).apply {
                            putExtras(bundleOf("ID_FROM_NOTIF" to recipe.id))
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )
            //.setStyleMediaStyle().setMediaSession(MediaSessionCompat.Token.fromToken(recipe)))

            builder.priority =
                NotificationCompat.PRIORITY_DEFAULT

            NotificationManagerCompat.from(app).notify(5, builder.build())
        }
    }


    @TestOnly
    fun createDummySample(): Recipe {

        currentRecipe = Recipe().apply {
            name = "Recipe For Testing"
            imageURL =
                "https://img.huffingtonpost.com/asset/5c92b00222000033001b332d.jpeg?ops=scalefit_630_noupscale"
            ingredients.add(
                IngredientForRecipe(
                    null,
                    2.0,
                    Ingredient.Unit.UNIT,
                    "Eggs",
                    "drawable://egg",
                    1,
                    null,
                    null
                )
            )
            ingredients.add(
                IngredientForRecipe(
                    null,
                    500.0,
                    Ingredient.Unit.GRAMME,
                    "Flour",
                    "drawable://flour",
                    2,
                    null,
                    null
                )
            )
            ingredients.add(
                IngredientForRecipe(
                    null,
                    200.0,
                    Ingredient.Unit.MILLILITER,
                    "Water",
                    "drawable://water",
                    3,
                    null,
                    null
                )
            )
            ingredients.add(
                IngredientForRecipe(
                    null,
                    2.33,
                    Ingredient.Unit.CUP,
                    "Butter",
                    "drawable://butter",
                    4,
                    null,
                    null
                )
            )

            steps.add(Step(null, "Step 1", Int.MAX_VALUE, null).apply { order = 1 })
            steps.add(Step(null, "Step 2", Int.MAX_VALUE, null).apply { order = 2 })
            steps.add(Step(null, "Step 3", Int.MAX_VALUE, null).apply {
                order = 3
                ingredients.add(
                    IngredientForRecipe(
                        null,
                        1.0,
                        Ingredient.Unit.TEASPOON,
                        "Salt",
                        "drawable://salt",
                        4,
                        null,
                        null
                    ).also {
                        it.step = this
                    }
                )

                ingredients.add(
                    IngredientForRecipe(
                        null,
                        1000.0,
                        Ingredient.Unit.TABLESPOON,
                        "Sugar",
                        "drawable://sugar",
                        4,
                        null,
                        null
                    ).also {
                        it.step = this
                    }
                )

                ingredients.add(
                    IngredientForRecipe(
                        null,
                        1000.0,
                        Ingredient.Unit.LITER,
                        "Milk",
                        "drawable://milk",
                        4,
                        null,
                        null
                    ).also {
                        it.step = this
                    }
                )
            })
        }

        return currentRecipe
    }

}
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.entities.IngredientForRecipe
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.StepEntity
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.repository.StepRepository
import zelgius.com.myrecipes.utils.PdfGenerator
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.myrecipes.worker.DownloadImageWorker
import zelgius.com.protobuff.RecipeProto
import java.io.File
import java.io.InputStream
import java.io.OutputStream


val TAG = RecipeViewModel::class.simpleName

class RecipeViewModel(val app: Application) : AndroidViewModel(app) {


    private val recipeRepository = RecipeRepository(app)
    private val ingredientRepository = IngredientRepository(app)
    private val stepRepository = StepRepository(app)

    val selectedRecipe = MutableLiveData<RecipeEntity>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri?>()

    private val _pdfProgress = MutableLiveData<Boolean>(false)
    val pdfProgress: LiveData<Boolean>
        get() = _pdfProgress

    val selectRecipe = MutableLiveData<Boolean>()

    var selectedType: RecipeEntity.Type = RecipeEntity.Type.MEAL

    var currentRecipe: RecipeEntity = RecipeEntity()

    private val _selection = mutableListOf<RecipeEntity>()
    val selection: List<RecipeEntity>
        get() = _selection

    val mealList = LivePagedListBuilder(recipeRepository.pagedMeal(), /* page size  */ 20).build()
    val dessertList =
        LivePagedListBuilder(recipeRepository.pagedDessert(), /* page size  */ 20).build()
    val otherList = LivePagedListBuilder(recipeRepository.pagedOther(), /* page size  */ 20).build()

    val ingredients: LiveData<List<IngredientEntity>>

    private val searchQuery = MutableLiveData<String>()
    val searchResult = searchQuery.switchMap{
        recipeRepository.pagedSearch(it).toLiveData(20)
    }

    init {
        ingredients = ingredientRepository.get()
    }

    fun search(query: String): LiveData<PagedList<RecipeEntity>> {
        searchQuery.value = query

        return searchResult
    }

    fun saveCurrentRecipe(): LiveData<RecipeEntity> =
        saveRecipe(currentRecipe)

    fun toggleSelectedItem(item: RecipeEntity) {
        if (!_selection.remove(item))
            _selection.add(item)
    }

    fun clearSelection() {
        _selection.clear()
    }


    fun saveRecipe(recipe: RecipeEntity): LiveData<RecipeEntity> {
        val done = MutableLiveData<RecipeEntity>()

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

    fun loadRecipe(id: Long): LiveData<RecipeEntity?> {
        val done = MutableLiveData<RecipeEntity?>()

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


    fun delete(recipe: RecipeEntity) = liveData {
        ingredientRepository.delete(recipe)

        stepRepository.delete(recipe)

        recipeRepository.delete(recipe)

        emit(true)

    }

    fun removeImage(recipe: RecipeEntity) {
        File(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${recipe.id}").delete()
    }

    private fun runInTransaction(runner: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                AppDatabase.getInstance(app).runInTransaction(runner)
            }
        }
    }

    fun exportSelectionToPdf(uri: Uri) = liveData {
        _pdfProgress.value = true

        with(PdfGenerator(app).createPdf(_selection.map {
            recipeRepository.getFull(it.id!!)!!
        }, uri)) {
            emit(this)
        }
        _pdfProgress.value = false
    }

    fun exportToPdf(recipe: RecipeEntity, uri: Uri) = liveData {
        _pdfProgress.value = true

        with(PdfGenerator(app).createPdf(recipe, uri)) {
            _pdfProgress.value = false
            emit(this)
        }
    }

    fun saveFromQrCode(scannedBase64: String): LiveData<RecipeEntity?> {

        val result = MutableLiveData<RecipeEntity?>()

        runInTransaction {
            viewModelScope.launch {
                result.value = try {
                    val bytes = Base64.decode(scannedBase64, Base64.NO_PADDING).unzip()

                    @Suppress("BlockingMethodInNonBlockingContext")
                    val proto =
                        coroutineScope {
                            RecipeProto.Recipe.parseFrom(bytes)
                        }
                    val recipe = RecipeEntity(proto)

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

    fun buildNotification(recipe: RecipeEntity) {
        val list = mutableListOf<Parcelable>()
        list.addAll(recipe.ingredients.filter { it.step == null }.sortedBy { it.sortOrder })
        recipe.steps.forEach { s ->
            list.addAll(recipe.ingredients.filter {
                with(it.step == s) {
                    if (this) it.optional = it.optional == true || s.optional

                    this
                }
            }.sortedBy { it.sortOrder })
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
                is StepEntity -> o.text
                is IngredientForRecipe -> IngredientForRecipe.text(app, o)
                else -> error("Should not be there")
            }.let {
                if (o is StepEntity && o.optional || o is IngredientForRecipe && (o.optional == true || o.step?.optional == true))
                    "($it)"
                else it
            }

            val drawable = when (o) {
                is StepEntity -> UiUtils.getDrawable(
                    app,
                    "${o.order}"
                )
                is IngredientForRecipe -> UiUtils.getDrawable(
                    app, if (!o.imageUrl.isNullOrEmpty()) o.imageUrl!! else o.name
                )
                else -> error("Should not be there")
            }

            val pIntent =
                PendingIntent.getBroadcast(
                    app,
                    1,
                    intentAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

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
                    PendingIntent.getActivity(
                        app, 0,
                        Intent(app, MainActivity::class.java).apply {
                            putExtras(bundleOf("ID_FROM_NOTIF" to recipe.id))
                        }, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            //.setStyleMediaStyle().setMediaSession(MediaSessionCompat.Token.fromToken(recipe)))

            builder.priority =
                NotificationCompat.PRIORITY_DEFAULT

            NotificationManagerCompat.from(app).notify(5, builder.build())
        }
    }

    fun copy(from: InputStream, to: OutputStream) = liveData {
        withContext(Dispatchers.IO) {
            from.copyTo(to)
        }

        emit(true)
    }

}
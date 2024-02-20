package zelgius.com.myrecipes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagingConfig
import androidx.paging.toLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.data.IngredientRepository
import zelgius.com.myrecipes.data.RecipeRepository
import zelgius.com.myrecipes.data.StepRepository
import zelgius.com.myrecipes.data.entities.RecipeEntity
import zelgius.com.myrecipes.data.entities.asModel
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.data.model.asEntity
import zelgius.com.myrecipes.data.repository.AppDatabase
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.utils.PdfGenerator
import zelgius.com.myrecipes.utils.UiUtils
import zelgius.com.myrecipes.utils.unzip
import zelgius.com.myrecipes.worker.DownloadImageWorker
import zelgius.com.protobuff.RecipeProto
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


val TAG = RecipeViewModel::class.simpleName

@HiltViewModel
class RecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val stepRepository: StepRepository,
) : ViewModel() {


    val selectedRecipe = MutableLiveData<Recipe>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri?>()

    private val _pdfProgress = MutableLiveData<Boolean>(false)
    val pdfProgress: LiveData<Boolean>
        get() = _pdfProgress

    val selectRecipe = MutableLiveData<Boolean>()

    var selectedType: Recipe.Type = Recipe.Type.Meal

    var currentRecipe: Recipe = Recipe(type = Recipe.Type.Meal, name = "")

    private val _selection = mutableListOf<Recipe>()
    val selection: List<Recipe>
        get() = _selection

    private val pageConfig = PagingConfig(
        pageSize = 10, // how many to load in each page
        prefetchDistance = 3, // how far from the end before we should load more; defaults to page size
        initialLoadSize = 10, // how many items should we initially load; defaults to 3x page size
    )


    val mealList = LivePagedListBuilder(recipeRepository.pagedMealLegacy, /* page size  */ 10).build()
    val dessertList =
        LivePagedListBuilder(recipeRepository.pagedDessertLegacy, /* page size  */ 10).build()
    val otherList = LivePagedListBuilder(recipeRepository.pagedOtherLegacy, /* page size  */ 10).build()

    val ingredients: LiveData<List<Ingredient>>

    private val searchQuery = MutableLiveData<String>()
    val searchResult = searchQuery.switchMap {
        recipeRepository.pagedSearch(it).toLiveData(20)
    }

    init {
        ingredients = ingredientRepository.get()
    }

    fun search(query: String): LiveData<PagedList<Recipe>> {
        searchQuery.value = query

        return searchResult
    }

    fun saveCurrentRecipe(): LiveData<Recipe> =
            saveRecipe(currentRecipe)


    fun toggleSelectedItem(item: Recipe) {
        if (!_selection.remove(item))
            _selection.add(item)
    }

    fun clearSelection() {
        _selection.clear()
    }


    fun saveRecipe(recipe: Recipe): LiveData<Recipe> {
        val done = MutableLiveData<Recipe>()

        runInTransaction {
            viewModelScope.launch {
                if (recipe.id == null)
                    recipeRepository.insert(recipe)
                else
                    recipeRepository.update(recipe)

                recipe.steps.forEach {
                    val step = it.copy(recipe = recipe)

                    if (it.id == null)
                        stepRepository.insert(step)
                    else stepRepository.update(step)
                }

                recipe.ingredients.forEach {
                    val ingredient = it.copy(
                        recipe = recipe,
                    )

                    if (it.id == null)
                        ingredientRepository.insert(ingredient, recipe)
                    else
                        ingredientRepository.update(ingredient)
                }

                ingredientRepository.deleteAllButThem(recipe, recipe.ingredients)
                stepRepository.deleteAllButThem(recipe, recipe.steps)

                done.value = recipe

                val worker = OneTimeWorkRequestBuilder<DownloadImageWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString("URL", recipe.imageUrl)
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

                selectedImageUrl.value = this?.imageUrl?.toUri()
                done.value = this
            }
        }

        return done
    }


    fun delete(recipe: Recipe) = liveData {
        ingredientRepository.delete(recipe)

        stepRepository.delete(recipe)

        recipeRepository.delete(recipe)

        emit(true)

    }

    fun removeImage(recipe: Recipe) {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${recipe.id}").delete()
    }

    private fun runInTransaction(runner: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                AppDatabase.getInstance(context).runInTransaction(runner)
            }
        }
    }

    fun exportSelectionToPdf(uri: Uri) = liveData {
        _pdfProgress.value = true

        with(PdfGenerator(context).createPdf(_selection.map {
            recipeRepository.getFull(it.id!!)!!.asEntity()
        }, uri)) {
            emit(this)
        }
        _pdfProgress.value = false
    }

    fun exportToPdf(recipe: RecipeEntity, uri: Uri) = liveData {
        _pdfProgress.value = true

        with(PdfGenerator(context).createPdf(recipe, uri)) {
            _pdfProgress.value = false
            emit(this)
        }
    }

    fun saveFromQrCode(scannedBase64: String): LiveData<Recipe?> {

        val result = MutableLiveData<Recipe?>()

        runInTransaction {
            viewModelScope.launch {
                result.value = try {
                    val bytes = Base64.decode(scannedBase64, Base64.NO_PADDING).unzip()

                    @Suppress("BlockingMethodInNonBlockingContext")
                    val proto =
                        coroutineScope {
                            RecipeProto.Recipe.parseFrom(bytes)
                        }
                    var recipe = RecipeEntity(proto)

                    recipe.ingredients.forEach {
                        it.id = ingredientRepository.get(it.name)?.id
                        it.step = recipe.steps.find { s -> s == it.step }
                    }

                    recipe = recipe.copy(id = recipeRepository.insert(recipe.asModel()))

                    val steps = recipe.steps.toList()
                    recipe.steps.clear()
                    recipe.steps.addAll(steps.map {
                        it.copy(
                            refRecipe = recipe.id,
                            id = stepRepository.insert(it.asModel(recipe.asModel()))
                        )
                    })

                    val ingredients = recipe.ingredients.toList()
                    recipe.ingredients.clear()

                    recipe.ingredients.addAll(ingredients.map {
                        val id = if (it.id == null)
                            ingredientRepository.insert(it.asModel(), recipe.asModel())
                        else {
                            ingredientRepository.update(it.asModel())
                            it.id
                        }

                        it.copy(
                            id = id ,
                            refRecipe = recipe.id,
                            refStep = it.step?.id
                        )
                    })

                    recipe.asModel()
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
            list.addAll(recipe.ingredients.filter {
                with(it.step == s) {
                    //if (this) it.optional = it.optional == true || s.optional

                    this
                }
            }.sortedBy { it.sortOrder })
            list.add(s)
        }

        if (list.isNotEmpty()) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.deleteNotificationChannel("recipe")
            notificationManager.createNotificationChannel(channel)

            //This is the intent of PendingIntent
            val intentAction = Intent(context, ActionBroadcastReceiver::class.java)

            //This is optional if you have more than one buttons and want to differentiate between two
            intentAction.putExtra("LIST", list.toTypedArray())
            intentAction.putExtra("INDEX", 0)
            intentAction.putExtra("TITLE", recipe.name)
            intentAction.putExtra("ID_FROM_NOTIF", recipe.id)

            val o = list.first()
            val text = when (o) {
                is Step -> o.text
                is Ingredient -> o.text(context)
                else -> error("Should not be there")
            }.let {
                if (o is Step && o.optional || o is Ingredient && (o.optional == true || o.step?.optional == true))
                    "($it)"
                else it
            }

            val drawable = when (o) {
                is Step -> UiUtils.getDrawable(
                    context,
                    "${o.order}"
                )

                is Ingredient -> UiUtils.getDrawable(
                    context, if (!o.imageUrl.isNullOrEmpty()) o.imageUrl!! else o.name
                )

                else -> error("Should not be there")
            }

            val pIntent =
                PendingIntent.getBroadcast(
                    context,
                    1,
                    intentAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            val builder = NotificationCompat.Builder(context, "recipe")
                .setSmallIcon(R.drawable.ic_restaurant_menu_black_24dp)
                .setContentTitle(recipe.name)
                .setContentText(text)
                .setLargeIcon(drawable!!.toBitmap())
                .setSound(null)
                .setVibrate(longArrayOf(0L))
                .addAction(
                    R.drawable.ic_skip_next_black_24dp,
                    context.getString(R.string.next),
                    pIntent
                )
                .setContentIntent(
                    PendingIntent.getActivity(
                        context, 0,
                        Intent(context, MainActivity::class.java).apply {
                            putExtras(bundleOf("ID_FROM_NOTIF" to recipe.id))
                        }, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            //.setStyleMediaStyle().setMediaSession(MediaSessionCompat.Token.fromToken(recipe)))

            builder.priority =
                NotificationCompat.PRIORITY_DEFAULT

            NotificationManagerCompat.from(context).notify(5, builder.build())
        }
    }

    fun copy(from: InputStream, to: OutputStream) = liveData {
        withContext(Dispatchers.IO) {
            from.copyTo(to)
        }

        emit(true)
    }

}
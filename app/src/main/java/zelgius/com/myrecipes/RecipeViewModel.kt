package zelgius.com.myrecipes

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.LivePagedListBuilder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.repository.IngredientRepository
import zelgius.com.myrecipes.repository.RecipeRepository
import zelgius.com.myrecipes.repository.StepRepository
import java.io.File
import android.os.Environment
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.myrecipes.repository.AppDatabase
import zelgius.com.myrecipes.worker.DownloadImageWorker

val TAG = RecipeViewModel::class.simpleName

class RecipeViewModel(val app: Application) : AndroidViewModel(app) {

    //protocol buffer
    var user: FirebaseUser? = null
        set(value) {
            field = value
            connectedUser.value = value
        }

    private val recipeRepository = RecipeRepository(app)
    private val ingredientRepository = IngredientRepository(app)
    private val stepRepository = StepRepository(app)

    val connectedUser = MutableLiveData<FirebaseUser?>()
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

    fun search(query: String) : LiveData<PagedList<Recipe>>{
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
                    it.refRecipe = currentRecipe.id

                    if (it.id == null)
                        it.id = stepRepository.insert(it)
                    else stepRepository.update(it)
                }

                recipe.ingredients.forEach {
                    it.refRecipe = currentRecipe.id
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


    fun newRecipe(recipe: Recipe): LiveData<Recipe> {
        val done = MutableLiveData<Recipe>()
        viewModelScope.launch {
            recipe.id = recipeRepository.insert(recipe)

            recipe.ingredients.forEach {
                if (it.id == null) it.id = ingredientRepository.insert(it, recipe)
            }

            done.value = recipe
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

            File(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${recipe.id}").delete()
        }

        return result
    }

    private fun runInTransaction(runner: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                AppDatabase.getInstance(app).runInTransaction(runner)
            }
        }
    }

    @TestOnly
    fun createDummySample() {

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
            })
        }
    }

}
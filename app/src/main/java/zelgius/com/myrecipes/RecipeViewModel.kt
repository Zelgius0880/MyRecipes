package zelgius.com.myrecipes

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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


class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    //protocol buffer
    var user: FirebaseUser? = null
        set(value) {
            field = value
            connectedUser.value = value
        }

    private val recipeRepository = RecipeRepository(application)
    private val ingredientRepository = IngredientRepository(application)
    private val stepRepository = StepRepository(application)

    val connectedUser = MutableLiveData<FirebaseUser?>()
    val selectedRecipe = MutableLiveData<Recipe>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri>()

    var currentRecipe: Recipe = Recipe()

    val mealList = LivePagedListBuilder(recipeRepository.pagedMeal(), /* page size  */ 20).build()
    val dessertList =
        LivePagedListBuilder(recipeRepository.pagedDessert(), /* page size  */ 20).build()
    val otherList = LivePagedListBuilder(recipeRepository.pagedOther(), /* page size  */ 20).build()

    val storageRef by lazy { FirebaseStorage.getInstance().reference }
    val ingredients: LiveData<List<Ingredient>>

    init {
        ingredients = ingredientRepository.get()
    }

    fun uploadFile(recipe: Recipe, file: File, callback: (Boolean) -> Unit = {}) {
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
    }

    fun saveCurrentRecipe(): LiveData<Boolean>{
        val done = MutableLiveData(false)

        val recipe = currentRecipe // thread safe assignement
        viewModelScope.launch {
            if(recipe.id == null)
                recipe.id = recipeRepository.insert(recipe)
            else
                recipeRepository.update(recipe)

            recipe.steps.forEach {
                it.refRecipe = currentRecipe.id
                it.id = stepRepository.insert(it)
            }

            recipe.ingredients.forEach {
                it.refRecipe = currentRecipe.id
                it.id = ingredientRepository.insert(it, recipe)
            }

            done.postValue(true)
        }

        return done
    }

    fun loadRecipe(id: Long): LiveData<Recipe?> {
        val done = MutableLiveData<Recipe>()

        viewModelScope.launch {
            currentRecipe = recipeRepository.getFull(id)
            done.value = currentRecipe
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

    @TestOnly
    fun createDummySample() {

        currentRecipe = Recipe().apply {
            name = "Recipe For Testing"
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
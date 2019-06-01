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
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.IngredientRepository
import zelgius.com.myrecipes.repository.RecipeRepository
import java.io.File


class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    var user: FirebaseUser? = null
        set(value) {
            field = value
            connectedUser.value = value
        }

    private val recipeRepository = RecipeRepository(application)
    private val ingredientRepository = IngredientRepository(application)
    val connectedUser = MutableLiveData<FirebaseUser?>()
    val selectedRecipe = MutableLiveData<Recipe>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri>()

    var currentRecipe: Recipe = Recipe()

    val mealList = LivePagedListBuilder(recipeRepository.pagedMeal(), /* page size  */ 20).build()
    val dessertList = LivePagedListBuilder(recipeRepository.pagedDessert(), /* page size  */ 20).build()
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


    fun newRecipe(recipe: Recipe): LiveData<Recipe>{
        val done = MutableLiveData<Recipe>()
        viewModelScope.launch {
            recipe.id = recipeRepository.insert(recipe)

            recipe.ingredients.forEach {
                if(it.id == null) it.id = ingredientRepository.insert(it, recipe)

            }
        }

        return done
    }
}
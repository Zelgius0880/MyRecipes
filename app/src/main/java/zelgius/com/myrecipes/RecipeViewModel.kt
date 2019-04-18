package zelgius.com.myrecipes

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe
import zelgius.com.myrecipes.repository.RecipeRepository
import java.io.File


class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    var user: FirebaseUser? = null
        set(value) {
            field = value
            connectedUser.value = value
        }

    val repository = RecipeRepository()
    val connectedUser = MutableLiveData<FirebaseUser?>()
    val selectedRecipe = MutableLiveData<Recipe>()
    val editMode = MutableLiveData<Boolean>()
    val selectedImageUrl = MutableLiveData<Uri>()

    val storageRef by lazy { FirebaseStorage.getInstance().reference }
    val ingredients = MutableLiveData<List<Ingredient>>()

    init {
        repository.getIngredients {
            ingredients.value = it
        }
    }

    fun uploadFile(recipe: Recipe, file: File, callback: (Boolean) -> Unit = {}) {
        val ref = storageRef.child("images/${recipe.key}.png")

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
}
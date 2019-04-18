package zelgius.com.myrecipes.repository

import com.google.firebase.firestore.FirebaseFirestore
import zelgius.com.myrecipes.entities.Ingredient
import zelgius.com.myrecipes.entities.Recipe

class RecipeRepository {

    private val database = AppDatabase.getInstance()

    fun getMealsQuery() =
            database
                .collection(Recipe.Type.MEAL.collection)
                .orderBy(Recipe.Fields.NAME.value)

    fun getDessertsQuery() =
        database
            .collection(Recipe.Type.DESSERT.collection)
            .orderBy(Recipe.Fields.NAME.value)

    fun getOthersQuery() =
        database
            .collection(Recipe.Type.OTHER.collection)
            .orderBy(Recipe.Fields.NAME.value)

    fun getIngredients(callback: (List<Ingredient>) -> Unit){
        database.collection("ingredients")
            .get()
            .addOnSuccessListener {
                callback(it.toObjects(Ingredient::class.java))
            }
            .addOnFailureListener {
                throw it
            }

    }

    fun createIngredient(ingredient: Ingredient){
        val data = HashMap<String, Any>()

        val key = database.collection("cities").document()

        key.set(data)
    }
}
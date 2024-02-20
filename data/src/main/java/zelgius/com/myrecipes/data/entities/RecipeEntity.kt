package zelgius.com.myrecipes.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.protobuff.RecipeProto
import java.io.File

/**
 * Represent a recipe. Can be a meal, a dessert or something else (other)
 *
 *
 * @property id Long?                                          the id of the recipe. Null if it's a new recipe
 * @property name String                                       the name of the recipe
 * @property imageURL String?                                  the imageURL -> probably a FireStore URL. Can be null
 * @property type Type                                         the type of the recipe. MEAL, DESSERT or OTHER
 * @property steps MutableList<Step>                           the list of the different steps of the recipe
 * @property ingredients MutableList<IngredientForRecipe>      the list of the ingredients used in the recipe
 * @property image File?                                       temporary file for containing the image when loaded from the device. Before being sent to Firestore
 * @constructor  Create a new recipe with no image file, no ingredients and no steps
 */
@Entity(tableName = "Recipe")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    @ColumnInfo(name = "image_url") val imageURL: String? = null,
    val type: Type = Type.MEAL,
    @Ignore
    val steps: MutableList<StepEntity> = mutableListOf(),
    @Ignore
    val ingredients: MutableList<IngredientForRecipe> = mutableListOf()
) {

    constructor(id: Long?, name: String, imageURL: String?, type: Type) : this(id, name, imageURL, type, mutableListOf(), mutableListOf())

    @Ignore
    constructor() : this(null, "", "", Type.OTHER)

    @Ignore
    constructor(type: Type) : this(null, "", "", type)

    @Ignore
    val image: File? = null

    @Ignore
    var isPinned: Boolean = false

    enum class Type {
        DESSERT,
        MEAL,
        OTHER
    }


    @Ignore
    constructor(recipe: RecipeProto.Recipe) : this(
        null,
        recipe.name,
        if (recipe.hasImageUrl()) recipe.imageUrl else null,
        Type.valueOf(recipe.type.name)
    ) {
        for(i in 0 until recipe.stepsCount)
            steps.add(StepEntity(recipe.getSteps(i)))

        for(i in 0 until recipe.ingredientsCount)
            ingredients.add(IngredientForRecipe(recipe.getIngredients(i)))
    }


    fun toProtoBuff(): RecipeProto.Recipe =
        RecipeProto.Recipe.newBuilder()
            .setName(name)
            .setType(RecipeProto.Recipe.Type.valueOf(type.name))
            .addAllSteps(steps.map {it.toProtoBuff()})
            .addAllIngredients(ingredients.map {it.toProtoBuff()}).also {
                if(imageURL != null) it.imageUrl = imageURL
            }
            .build()!!
}


fun RecipeEntity.Type.asModel() = when(this) {
    RecipeEntity.Type.DESSERT -> Recipe.Type.Dessert
    RecipeEntity.Type.MEAL -> Recipe.Type.Meal
    RecipeEntity.Type.OTHER -> Recipe.Type.Other
}

fun RecipeEntity.asModel(): Recipe {
    val steps = steps.map { it.asModel() }
    val ingredients = ingredients.map { it.asModel(
        step = steps.find { s -> s.id == it.refStep }
    ) }

    return Recipe(
        name = name,
        id = id,
        imageUrl = imageURL,
        type = type.asModel(),
        steps = steps,
        ingredients = ingredients
    )
}
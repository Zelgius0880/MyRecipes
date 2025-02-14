package zelgius.com.myrecipes.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.protobuff.RecipeProto
import java.io.File


@Entity(tableName = "Recipe")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    @ColumnInfo(name = "image_url") val imageURL: String? = null,
    val type: Type = Type.MEAL,
    @ColumnInfo(name = "seed") var seed: Int? = null,
    @ColumnInfo(name = "prompt") var prompt: String? = null,
    @ColumnInfo(name = "generation_enabled", defaultValue = "1") var generationEnabled: Boolean = true,
    @Ignore
    val steps: MutableList<StepEntity> = mutableListOf(),
    @Ignore
    val ingredients: MutableList<IngredientForRecipe> = mutableListOf(),
) {

    constructor(id: Long?, name: String, imageURL: String?, type: Type) : this(
        id = id,
        name = name,
        imageURL = imageURL,
        type = type,
        steps = mutableListOf(),
    )

    @Ignore
    constructor() : this(null, "", "", Type.OTHER)

    @Ignore
    constructor(type: Type) : this(null, "", "", type)

    @Ignore
    val image: File? = null

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
        for (i in 0 until recipe.stepsCount)
            steps.add(StepEntity(recipe.getSteps(i)))

        for (i in 0 until recipe.ingredientsCount)
            ingredients.add(IngredientForRecipe(recipe.getIngredients(i)))
    }


    fun toProtoBuff(): RecipeProto.Recipe =
        RecipeProto.Recipe.newBuilder()
            .setName(name)
            .setType(RecipeProto.Recipe.Type.valueOf(type.name))
            .addAllSteps(steps.map { it.toProtoBuff() })
            .addAllIngredients(ingredients.map { it.toProtoBuff() }).also {
                if (imageURL != null) it.imageUrl = imageURL
            }
            .build()!!
}


fun RecipeEntity.Type.asModel() = when (this) {
    RecipeEntity.Type.DESSERT -> Recipe.Type.Dessert
    RecipeEntity.Type.MEAL -> Recipe.Type.Meal
    RecipeEntity.Type.OTHER -> Recipe.Type.Other
}

fun RecipeEntity.asModel(): Recipe {
    val steps = steps.map { it.asModel() }
    val ingredients = ingredients.map {
        it.asModel(
            step = steps.find { s -> s.id == it.refStep }
        )
    }

    return Recipe(
        name = name,
        id = id,
        imageUrl = imageURL,
        type = type.asModel(),
        steps = steps,
        ingredients = ingredients,
        seed = seed,
        prompt = prompt,
        generationEnabled = generationEnabled
    )
}

@Entity
class RecipeImageUrlUpdate(
    var id: Long? = null,
    @ColumnInfo(name = "image_url") var imageURL: String? = null
)
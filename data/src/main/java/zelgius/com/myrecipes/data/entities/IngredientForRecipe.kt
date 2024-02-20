package zelgius.com.myrecipes.data.entities

import androidx.room.DatabaseView
import androidx.room.Ignore
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.protobuff.RecipeProto

@DatabaseView(
    """
SELECT ri.quantity, ri.unit, ri.ref_recipe AS refRecipe, ri.ref_step AS refStep, ri.sort_order AS sortOrder,
i.name, i.id, i.image_url AS imageUrl, ri.optional FROM RecipeIngredient ri
INNER JOIN Ingredient i ON i.id = ri.ref_ingredient
        """
)
data class IngredientForRecipe(
    var id: Long?,
    var quantity: Double,
    var unit: IngredientEntity.Unit,
    var name: String,
    var imageUrl: String?,
    var optional: Boolean?,
    var sortOrder: Int,
    var refRecipe: Long?,
    var refStep: Long?
) {

    @Ignore
    var new = false

    @Ignore
    var step: StepEntity? = null

    @Ignore
    constructor(
        id: Long?,
        quantity: Double,
        unit: IngredientEntity.Unit,
        name: String,
        imageUrl: String?,
        sortOrder: Int,
        refRecipe: Long?,
        refStep: Long?
    ) : this(id, quantity, unit, name, imageUrl, false, sortOrder, refRecipe, refStep)


    @Ignore
    constructor(ingredient: RecipeProto.Ingredient) : this(
        null,
        ingredient.quantity,
        IngredientEntity.Unit.valueOf(ingredient.unit.name),
        ingredient.name,
        if (ingredient.hasImageUrl()) ingredient.imageUrl else null,
        false,
        ingredient.sortOrder,
        null,
        null
    ) {
        if (ingredient.hasStep()) {
            step = StepEntity(ingredient.step)
        }
    }


    fun toProtoBuff() = RecipeProto.Ingredient.newBuilder()
        .setName(name)
        .setQuantity(quantity)
        .setSortOrder(sortOrder)
        .setIsOptional(optional?:false)
        .setUnit(RecipeProto.Ingredient.Unit.valueOf(unit.name))
        .also {
            if (step != null) it.step = step?.toProtoBuff()
            if (imageUrl != null) it.imageUrl = imageUrl
        }
        .build()!!
}

fun IngredientForRecipe.asModel(recipe: Recipe? = null, step: zelgius.com.myrecipes.data.model.Step? = null) = Ingredient(
    id = id,
            quantity = quantity,
            unit = unit.asModel(),
            name = name,
            imageUrl = imageUrl,
            optional = optional,
            sortOrder = sortOrder,
            recipe = recipe,
            step = step,
)
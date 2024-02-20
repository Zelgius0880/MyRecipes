package zelgius.com.myrecipes.data.entities

import androidx.room.*
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.protobuff.RecipeProto


/**
 * Step of a recipe
 *
 * @property id Long?           id in database. Null if new one
 * @property text String        the description of the step
 * @property order Int          the order of the step
 * @property refRecipe Long?    the id of the referenced recipe
 * @constructor     Create from a parcel
 */
@Entity(
    tableName = "Step",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            childColumns = ["ref_recipe"], parentColumns = ["id"]
        )
    ],
    indices = [Index(value = ["ref_recipe"])]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val text: String,
    val order: Int = Int.MAX_VALUE,
    val optional: Boolean,
    @ColumnInfo(name = "ref_recipe") val refRecipe: Long?
) {

    @Ignore
    var new = false

    @Ignore
    constructor(step: RecipeProto.Step) : this(
        null,
        step.name,
        step.order,
        false,
        null
    )



    fun toProtoBuff() = RecipeProto.Step.newBuilder()
        .setName(text)
        .setOrder(order)
        .build()!!
}

fun StepEntity.asModel(recipe: Recipe? = null) = Step(
    id = id,
    text = text,
    order = order,
    optional = optional,
    recipe = recipe,
)
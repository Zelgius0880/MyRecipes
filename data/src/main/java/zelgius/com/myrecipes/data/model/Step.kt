package zelgius.com.myrecipes.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import zelgius.com.myrecipes.data.entities.StepEntity

@Parcelize
data class Step(
    val id: Long? = null,
    val text: String,
    val order: Int = Int.MAX_VALUE,
    val optional: Boolean = false,
    val recipe: Recipe?
) : Parcelable

fun Step.asEntity() = StepEntity(
    id = id,
    text = text,
    order = order,
    optional = optional,
    refRecipe = recipe?.id,
)



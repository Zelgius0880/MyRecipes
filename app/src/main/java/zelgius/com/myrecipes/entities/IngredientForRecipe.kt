package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.DatabaseView
import androidx.room.Ignore

@DatabaseView(
    """
    SELECT ri.quantity, ri.unit, ri.ref_recipe AS refRecipe, ri.ref_step AS refStep, ri.sort_order AS sortOrder,
     i.name, i.id, i.image_url AS imageUrl FROM RecipeIngredient ri
        INNER JOIN Ingredient i ON i.id = ri.ref_recipe
        """
)
data class IngredientForRecipe(
    var id: Long?,
    var quantity: Double,
    var unit: Ingredient.Unit,
    var name: String,
    var imageUrl: String?,
    var sortOrder: Int,
    var refRecipe: Long?,
    var refStep: Long?
) : Parcelable {

    @Ignore
    var new = false

    @Ignore
    var step: Step? = null

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readDouble(),
        Ingredient.Unit.valueOf(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readInt(),
        parcel.readLong().let { if (it >= 0) it else null },
        parcel.readLong().let { if (it >= 0) it else null }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeDouble(quantity)
        parcel.writeString(unit.name)
        parcel.writeString(name)
        parcel.writeString(imageUrl)
        parcel.writeInt(sortOrder)
        parcel.writeLong(refRecipe ?: -1)
        parcel.writeLong(refStep ?: -1)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IngredientForRecipe> {
        override fun createFromParcel(parcel: Parcel): IngredientForRecipe {
            return IngredientForRecipe(parcel)
        }

        override fun newArray(size: Int): Array<IngredientForRecipe?> {
            return arrayOfNulls(size)
        }
    }
}
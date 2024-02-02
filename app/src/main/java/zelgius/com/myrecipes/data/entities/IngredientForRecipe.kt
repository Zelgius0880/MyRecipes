package zelgius.com.myrecipes.data.entities

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.core.os.ParcelCompat
import androidx.room.DatabaseView
import androidx.room.Ignore
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.utils.round
import zelgius.com.protobuff.RecipeProto
import java.text.DecimalFormat

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
) : Parcelable {

    @Ignore
    var new = false

    @Ignore
    var step: StepEntity? = null

    @get:DrawableRes
    val drawable: Int? get() = DefaultIngredients.entries.firstOrNull { it.url == imageUrl }?.drawable
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
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readDouble(),
        IngredientEntity.Unit.valueOf(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString(),
        ParcelCompat.readBoolean(parcel),
        parcel.readInt(),
        parcel.readLong().let { if (it >= 0) it else null },
        parcel.readLong().let { if (it >= 0) it else null }
    )

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeDouble(quantity)
        parcel.writeString(unit.name)
        parcel.writeString(name)
        parcel.writeString(imageUrl)
        ParcelCompat.writeBoolean(parcel, optional?:false)
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

        fun text(context: Context, item: IngredientForRecipe): String = item.text(context)
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

fun IngredientForRecipe.text(context: Context): String {
    val abrv = when (unit) {
        IngredientEntity.Unit.MILLILITER -> context.getString(R.string.milliliter_abrv)
        IngredientEntity.Unit.LITER -> context.getString(R.string.liter_abrv)
        IngredientEntity.Unit.UNIT -> context.getString(R.string.unit_abrv)
        IngredientEntity.Unit.TEASPOON -> context.getString(R.string.teaspoon_abrv)
        IngredientEntity.Unit.TABLESPOON -> context.getString(R.string.tablespoon_abrv)
        IngredientEntity.Unit.GRAMME -> context.getString(R.string.gramme_abrv)
        IngredientEntity.Unit.KILOGRAMME -> context.getString(R.string.kilogramme_abrv)
        IngredientEntity.Unit.CUP -> context.getString(R.string.cup_abrv)
        IngredientEntity.Unit.PINCH -> context.getString(R.string.pinch_abrv)
    }

    return if (unit != IngredientEntity.Unit.CUP) {
        String.format(
            "%s %s %s",
            DecimalFormat("#0.##").format(quantity),
            abrv,
            name
        )
    } else {
        val part1 = if (quantity.toInt() == 0) "" else "${quantity.toInt()} "

        val part2 = when ("${(quantity - quantity.toInt()).round(2)}".trim()) {
            "0.0", "0" -> ""
            "0.33", "0.34" -> "1/3 "
            "0.66", "0.67" -> "2/3 "
            "0.25" -> "1/4 "
            "0.5" -> "1/2 "
            "0.75" -> "3/4 "
            else -> "${DecimalFormat("#0.##").format(quantity - quantity.toInt())} "
        }

        String.format("%s%s%s %s", part1, part2, abrv, name)
    }
}
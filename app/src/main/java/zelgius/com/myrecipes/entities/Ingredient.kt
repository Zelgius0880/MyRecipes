package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*

@Entity(
    foreignKeys = [
        ForeignKey(entity = Ingredient::class, parentColumns = ["id"], childColumns = ["ref_ingredient"]),
        ForeignKey(entity = Ingredient::class, parentColumns = ["id"], childColumns = ["ref_recipe"])
    ],
    indices = [
        Index(value = ["ref_ingredient"]),
        Index(value = ["ref_recipe"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var quantity: Double,
    var unit: Ingredient.Unit,
    @ColumnInfo(name = "ref_ingredient") var refIngredient: Long?,
    @ColumnInfo(name = "ref_recipe") var refRecipe: Long?
) {
    //constructor() : this (null, 0, Ingredient.Unit.GRAMME, null, null)
}

@Entity
data class Ingredient(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var name: String,
    @ColumnInfo(name = "image_url") var imageURL: String?

) : Parcelable {

    //constructor() : this (null, "", null)

    enum class Unit {
        MILLILITER,
        LITER,
        UNIT,
        TEASPOON,
        TABLESPOON,
        GRAMME,
        KILOGRAMME
    }


    /**
     * Parcelable
     */
    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id?:0)
        parcel.writeString(name)
        parcel.writeString(imageURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ingredient> {
        override fun createFromParcel(parcel: Parcel): Ingredient {
            return Ingredient(parcel)
        }

        override fun newArray(size: Int): Array<Ingredient?> {
            return arrayOfNulls(size)
        }
    }
}

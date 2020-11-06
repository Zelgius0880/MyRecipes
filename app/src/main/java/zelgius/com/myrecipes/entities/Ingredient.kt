package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.*
import zelgius.com.myrecipes.R

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ref_ingredient"]
        ),
        ForeignKey(entity = Recipe::class, parentColumns = ["id"], childColumns = ["ref_recipe"]),
        ForeignKey(entity = Step::class, parentColumns = ["id"], childColumns = ["ref_step"])
    ],
    indices = [
        Index(value = ["ref_ingredient"]),
        Index(value = ["ref_recipe"]),
        Index(value = ["ref_step"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var quantity: Double,
    var unit: Ingredient.Unit,
    var optional: Boolean?,
    @ColumnInfo(name = "sort_order") var sortOrder: Int,
    @ColumnInfo(name = "ref_ingredient") var refIngredient: Long?,
    @ColumnInfo(name = "ref_recipe") var refRecipe: Long?,
    @ColumnInfo(name = "ref_step") var refStep: Long?
)

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
        KILOGRAMME,
        CUP,
        PINCH
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
        parcel.writeLong(id ?: 0)
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

    override fun toString(): String {
        return name
    }
}


enum class DefaultIngredients(@StringRes val string: Int, val url: String, @DrawableRes val drawable: Int) {
    EGG(R.string.egg_name, "drawable://egg", R.drawable.ic_eggs),
    FLOUR(R.string.flour_name, "drawable://flour", R.drawable.ic_flour),
    SUGAR(R.string.sugar_name, "drawable://sugar", R.drawable.ic_suggar),
    WATER(R.string.water_name, "drawable://water", R.drawable.ic_drop),
    MILK(R.string.milk_name, "drawable://milk", R.drawable.ic_milk),
    BUTTER(R.string.butter_name, "drawable://butter", R.drawable.ic_butter),
    SALT(R.string.salt_name, "drawable://salt", R.drawable.ic_salt),
    APPLE(R.string.apple_name, "drawable://apple", R.drawable.ic_apple),
    AVOCADO(R.string.avocado_name, "drawable://avocado", R.drawable.ic_avocado),
    BROWN_SUGAR(R.string.brown_sugar_name, "drawable://brown_sugar", R.drawable.ic_brown_suggar),
    CHEESE(R.string.cheese_name, "drawable://cheese", R.drawable.ic_cheese),
    CHOCOLATE(R.string.chocolate_name, "drawable://chocolate", R.drawable.ic_chocolate),
    COCONUT(R.string.coconut_name, "drawable://coconut", R.drawable.ic_coconut),
    COFFEE(R.string.coffee_name, "drawable://coffee", R.drawable.ic_coffee),
    HAZELNUT(R.string.hazelnut_name, "drawable://hazelnut", R.drawable.ic_hazelnut),
    HONEY(R.string.honey_name, "drawable://honey", R.drawable.ic_honey),
    NUTS(R.string.nuts_name, "drawable://nuts", R.drawable.ic_nuts),
    PEANUT(R.string.peanut_name, "drawable://peanut", R.drawable.ic_peanut),
    PEAR(R.string.pear_name, "drawable://pear", R.drawable.ic_pear),
    PEPPER(R.string.pepper_name, "drawable://pepper", R.drawable.ic_pepper),
    POTATO(R.string.potato_name, "drawable://potatoe", R.drawable.ic_potatoes),
    RASPBERRY(R.string.raspberry_name, "drawable://raspberry", R.drawable.ic_raspberry),
    STRAWBERRY(R.string.strawberry_name, "drawable://strawberry", R.drawable.ic_strawberry),
    TOMATO(R.string.tomato_name, "drawable://tomato", R.drawable.ic_tomato),
    TOMATO_SAUCE(R.string.tomato_sauce_name, "drawable://tomato_sauce", R.drawable.ic_tomato_sauce),
}
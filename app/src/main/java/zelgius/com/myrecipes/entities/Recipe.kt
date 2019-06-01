package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
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
@Entity
data class Recipe (
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var name: String,
    @ColumnInfo(name = "image_url") var imageURL: String?,
    var type: Type
): Parcelable{


    @Ignore
    constructor() : this(null, "", "", Type.OTHER)

    @Ignore
    constructor(type: Type) : this(null, "", "", type)

    @Ignore
    val steps: MutableList<Step> = mutableListOf()

    @Ignore
    val ingredients: MutableList<IngredientForRecipe> = mutableListOf()

    @Ignore
    val image: File? = null

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readLong().let { if(it >= 0) it else null},
        parcel.readString()!!,
        parcel.readString(),
        Type.valueOf(parcel.readString()!!)
    ) {
        parcel.readTypedList(ingredients, IngredientForRecipe.CREATOR)
        parcel.readTypedList(steps, Step.CREATOR)
    }

    enum class Type {
        DESSERT,
        MEAL,
        OTHER
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id?: -1)
        parcel.writeString(name)
        parcel.writeString(imageURL)
        parcel.writeString(type.name)

        parcel.writeTypedList(ingredients)
        parcel.writeTypedList(steps)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }
}

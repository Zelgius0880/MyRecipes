package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.File

@Entity
data class Recipe (
    var id: Long?,
    var name: String,
    @ColumnInfo(name = "image_url") var imageURL: String?,
    var type: Type
): Parcelable{

    @Ignore
    constructor(type: Type) : this(null, "", "", type)

    @Ignore
    val steps: List<Step> = mutableListOf()

    @Ignore
    val ingredients: List<Ingredient> = mutableListOf()

    @Ignore
    val image: File? = null

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readLong().let { if(it >= 0) it else null},
        parcel.readString()!!,
        parcel.readString(),
        Type.valueOf(parcel.readString()!!)
    ) {
        parcel.readTypedList(ingredients, Ingredient.CREATOR)
        parcel.readTypedList(steps, Step.CREATOR)
    }

    enum class Type(val collection: String){
        DESSERT ("desserts"),
        MEAL("meals"),
        OTHER("others")
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

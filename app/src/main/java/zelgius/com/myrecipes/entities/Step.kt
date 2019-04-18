package zelgius.com.myrecipes.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            childColumns = ["ref_recipe"], parentColumns = ["id"]
        )
    ],
    indices = [Index(value = ["ref_recipe"])]
)
data class Step(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var text: String,
    var order: Int = Int.MAX_VALUE,
    @ColumnInfo(name = "ref_recipe") var refRecipe: Long?
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readLong().let { if (it >= 0) it else null },
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readLong().let { if (it >= 0) it else null }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id ?: -1)
        parcel.writeString(text)
        parcel.writeInt(order)
        parcel.writeLong(refRecipe ?: -1)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(parcel: Parcel): Step {
            return Step(parcel)
        }

        override fun newArray(size: Int): Array<Step?> {
            return arrayOfNulls(size)
        }
    }
}
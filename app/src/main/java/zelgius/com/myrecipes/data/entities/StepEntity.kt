package zelgius.com.myrecipes.data.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import androidx.room.*
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
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var text: String,
    var order: Int = Int.MAX_VALUE,
    var optional: Boolean,
    @ColumnInfo(name = "ref_recipe") var refRecipe: Long?
) : Parcelable {

    @Ignore
    var new = false


    constructor(parcel: Parcel) : this(
        parcel.readLong().let { if (it >= 0) it else null },
        parcel.readString()!!,
        parcel.readInt(),
        ParcelCompat.readBoolean(parcel),
        parcel.readLong().let { if (it >= 0) it else null }
    )

    @Ignore
    constructor(step: RecipeProto.Step) : this(
        null,
        step.name,
        step.order,
        false,
        null
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id ?: -1)
        parcel.writeString(text)
        parcel.writeInt(order)
        ParcelCompat.writeBoolean(parcel, optional)
        parcel.writeLong(refRecipe ?: -1)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StepEntity> {
        override fun createFromParcel(parcel: Parcel): StepEntity {
            return StepEntity(parcel)
        }

        override fun newArray(size: Int): Array<StepEntity?> {
            return arrayOfNulls(size)
        }
    }


    fun toProtoBuff() = RecipeProto.Step.newBuilder()
        .setName(text)
        .setOrder(order)
        .build()!!
}
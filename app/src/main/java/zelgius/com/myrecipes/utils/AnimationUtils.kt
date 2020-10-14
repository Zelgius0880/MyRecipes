package zelgius.com.myrecipes.utils

import android.os.Parcel
import android.os.Parcelable


object AnimationUtils {
    const val EXTRA_CIRCULAR_REVEAL_SETTINGS = "EXTRA_CIRCULAR_REVEAL_SETTINGS"

    class RevealAnimationSetting(
        private val centerX: Int,
        private val centerY: Int,
        val width: Int,
        val height: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(centerX)
            parcel.writeInt(centerY)
            parcel.writeInt(width)
            parcel.writeInt(height)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<RevealAnimationSetting> {
            override fun createFromParcel(parcel: Parcel): RevealAnimationSetting {
                return RevealAnimationSetting(parcel)
            }

            override fun newArray(size: Int): Array<RevealAnimationSetting?> {
                return arrayOfNulls(size)
            }
        }
    }


}
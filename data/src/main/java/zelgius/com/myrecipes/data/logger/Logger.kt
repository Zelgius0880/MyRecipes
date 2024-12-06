package zelgius.com.myrecipes.data.logger

import android.util.Log
import zelgius.com.myrecipes.data.BuildConfig


object Logger {
    val TAG = Logger::class.simpleName?:"Logger"
    fun i(tag:String, message: String) {
        if(BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun i(message: String) {
        i(TAG, message)
    }

    fun w(tag:String, message: String) {
        if(BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun w(message: String) {
        w(TAG, message)
    }
}
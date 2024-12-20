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

    fun e(message: String) {
        e(message)
    }

    fun e(message: String? = null, throwable: Throwable? = null) {
        if(BuildConfig.DEBUG) Log.e(TAG, message, throwable)
    }
}
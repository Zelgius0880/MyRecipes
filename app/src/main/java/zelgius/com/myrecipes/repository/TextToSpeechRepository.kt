package zelgius.com.myrecipes.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val textToSpeech: TextToSpeech = TextToSpeech(context) {
        if (it == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.getDefault()
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH): String {
        val uid = UUID.randomUUID().toString()
        textToSpeech.speak(text, queueMode, null, uid)
        return uid
    }

}
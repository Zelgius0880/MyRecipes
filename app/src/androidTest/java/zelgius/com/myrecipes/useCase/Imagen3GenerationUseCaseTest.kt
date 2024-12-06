package zelgius.com.myrecipes.useCase

import android.app.Application
import android.graphics.Bitmap
import android.os.Environment.DIRECTORY_PICTURES
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.functions
import com.zelgius.myrecipes.ia.usecase.Imagen3GenerationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import zelgius.com.myrecipes.TestDataBase
import zelgius.com.myrecipes.data.logger.Logger
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class Imagen3GenerationUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            val appContext = ApplicationProvider.getApplicationContext<Application>()!!
            FirebaseApp.initializeApp(appContext)
            Firebase.functions.useEmulator("10.0.2.2", 5001)
            TestDataBase.createDb()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            TestDataBase.closeDb()
        }
    }


    @Test
    fun generatingImage() = runBlocking {

        val useCase = Imagen3GenerationUseCase()
        val images = useCase.execute(
            "Cinnamon",
        )

        assert(images.isNotEmpty())
        val context = InstrumentationRegistry.getInstrumentation().context

        images.forEachIndexed { index, image ->
            val targetFile =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(
                        context.getExternalFilesDir(DIRECTORY_PICTURES),
                        "TestImage $index"
                    )
                )

            val output = context.contentResolver.openOutputStream(targetFile)

            if (output != null) {
                image.compress(Bitmap.CompressFormat.PNG, 100, output)
            }

            output?.close()

            Logger.i("Saved image to $targetFile")

            Runtime.getRuntime().exec("adb pull ${targetFile.path} ./test_images$index.png")
        }

    }
}
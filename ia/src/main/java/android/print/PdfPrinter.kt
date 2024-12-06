package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID

suspend fun WebView.pdf(): ByteArray {
    val jobName = "webpage"
    val attributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

    val printAdapter = createPrintDocumentAdapter(jobName)

    val file = File(context.filesDir, "temp_${UUID.randomUUID()}.pdf").apply {
        createNewFile()
    }
    val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)

    return suspendCancellableCoroutine { continuation ->
        printAdapter.onLayout(null, attributes, null, object :
            PrintDocumentAdapter.LayoutResultCallback() {
            override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                printAdapter.onWrite(
                    arrayOf(PageRange.ALL_PAGES),
                    parcelFileDescriptor,
                    CancellationSignal(),
                    object : PrintDocumentAdapter.WriteResultCallback() {
                        override fun onWriteFailed(error: CharSequence?) {
                            continuation.resumeWith(Result.failure(Exception(error.toString())))
                        }

                        override fun onWriteFinished(pages: Array<out PageRange>) {
                            if (pages.isNotEmpty()) {
                                continuation.resumeWith(Result.success(file.readBytes()))
                            } else {
                                continuation.resumeWith(Result.failure(Exception("No pages")))
                            }

                        }
                    })
            }
        }, null)
    }.apply {
        file.delete()
    }
}

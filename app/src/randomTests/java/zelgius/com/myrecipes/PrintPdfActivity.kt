package zelgius.com.myrecipes

import android.os.Bundle
import android.print.pdf
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class PrintPdfActivity : ComponentActivity() {
    private lateinit var printWeb: WebView
    private var printJob: android.print.PrintJob? = null
    private var printBtnPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold { padding ->
                Column (modifier = Modifier.padding(padding), horizontalAlignment = Alignment.CenterHorizontally){
                    WebView(modifier = Modifier.weight(1f), url = "https://www.marmiton.org/recettes/recette_la-vraie-tartiflette_17634.aspx"){
                         object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                printWeb = it
                            }
                        }
                    }
                    Button(onClick = {
                        lifecycleScope.launch {
                            val bytes = printWeb.pdf()
                            File(filesDir, "pdf.pdf").writeBytes(bytes)
                        }
                    }) {
                        Text("Print")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (printJob != null && printBtnPressed) {
            when {
                printJob!!.isCompleted -> {
                    Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show()
                }
                printJob!!.isStarted -> {
                    Toast.makeText(this, "isStarted", Toast.LENGTH_SHORT).show()
                }
                printJob!!.isBlocked -> {
                    Toast.makeText(this, "isBlocked", Toast.LENGTH_SHORT).show()
                }
                printJob!!.isCancelled -> {
                    Toast.makeText(this, "isCancelled", Toast.LENGTH_SHORT).show()
                }
                printJob!!.isFailed -> {
                    Toast.makeText(this, "isFailed", Toast.LENGTH_SHORT).show()
                }
                printJob!!.isQueued -> {
                    Toast.makeText(this, "isQueued", Toast.LENGTH_SHORT).show()
                }
            }

            printBtnPressed = false
        }
    }


    @Composable
    fun WebView(url: String, modifier: Modifier = Modifier, webViewClientBuilder: ((WebView) -> WebViewClient)? = null){
        AndroidView(modifier = modifier, factory = {
            WebView(it).apply {
                webViewClientBuilder?.invoke(this)?.let { webViewClient ->
                    this.webViewClient = webViewClient
                }
                this.webViewClient = webViewClient
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }, update = {
            it.loadUrl(url)
        })
    }
}
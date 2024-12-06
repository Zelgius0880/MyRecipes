package zelgius.com.myrecipes.ui.addFromWeb

import android.print.pdf
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.MainActivity.Navigation
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.addFromWeb.viewModel.AddFromWebViewModel
import zelgius.com.myrecipes.ui.common.AppTextField
import android.graphics.Color as AColor

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddFromWeb(
    navigator: ThreePaneScaffoldNavigator<Navigation>,
    type: Recipe.Type,
    viewModel: AddFromWebViewModel = hiltViewModel()
) {
    val url by viewModel.url.collectAsState()
    val context = LocalContext.current
    val loading = viewModel.loading.collectAsState()

    DisposableEffect(null) {
        onDispose {
            viewModel.onDispose()
        }
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(snackbarHost = {
        SnackbarHost(snackbarHostState) {
            ErrorSnackbar(it)
        }
    }) { padding ->

        @OptIn(ExperimentalMaterial3AdaptiveApi::class)
        suspend fun startExtraction(bytes: ByteArray) {
            viewModel.startExtraction(bytes, context.resources.configuration.locales[0])?.let {
                navigator.navigateTo(
                    ListDetailPaneScaffoldRole.Detail,
                    Navigation.Edit(it.copy(type = type)),
                )
            } ?: run {
                val action = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.data_extraction_error),
                    duration = SnackbarDuration.Long,
                    actionLabel = context.getString(R.string.retry)
                )

                if (action == SnackbarResult.ActionPerformed) {
                    startExtraction(bytes)
                }
            }
        }

        AddFromWeb(
            modifier = Modifier.padding(padding),
            url = url,
            navigateBack = {
                coroutineScope.launch {
                    navigator.navigateBack()
                }
            },
            onUrlChanged = viewModel::onUrlChanged,
            loading = loading.value,
            onExtract = {
                startExtraction(it)
            })

    }
}

@Composable
private fun AddFromWeb(
    modifier: Modifier = Modifier,
    url: String = "",
    loading: Boolean = false,
    onUrlChanged: (String) -> Unit = {},
    onExtract: suspend (ByteArray) -> Unit = {},
    navigateBack: (() -> Unit)? = null,
    @ColorInt webViewBackgroundColor: Int = AColor.TRANSPARENT
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var webView: WebView = remember {
        WebView(context).apply {
            setBackgroundColor(webViewBackgroundColor)
        }
    }

    Box(
        modifier = modifier
    ) {
        WebView(webView = webView, url = url, modifier = Modifier.fillMaxWidth()) {

            object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navigateBack != null) FilledIconButton(
                modifier = Modifier.padding(start = 8.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ), onClick = {
                    navigateBack()
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = ""
                )
            }
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                )

                AppTextField(
                    value = url,
                    onValueChange = {
                        onUrlChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.TwoTone.Close,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)

                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .clickable {
                                    onUrlChanged("")
                                })
                    },
                    singleLine = true,
                    label = {
                        Text(text = stringResource(R.string.recipe_url))
                    },
                )

            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    onExtract(webView.pdf())
                }
            },
            Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
                .fillMaxWidth(),
            enabled = url.isNotEmpty() && !loading,
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text(text = stringResource(R.string.save))
        }
    }
}

@Composable
private fun WebView(
    webView: WebView,
    url: String,
    modifier: Modifier = Modifier,
    webViewClientBuilder: ((WebView) -> WebViewClient)? = null
) {
    AndroidView(modifier = modifier, factory = {
        webView.apply {
            webViewClientBuilder?.invoke(this)?.let { webViewClient ->
                this.webViewClient = webViewClient
            }
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = {
        if(URLUtil.isValidUrl(url))
        it.loadUrl(url)
    })
}

@Composable
private fun ErrorSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        snackbarData = snackbarData,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        actionColor = MaterialTheme.colorScheme.error
    )
}

@Composable
@Preview
private fun ErrorSnackbarPreview() {
    val snackbarData = object : SnackbarData {
        override val visuals: SnackbarVisuals
            get() = object : SnackbarVisuals {
                override val actionLabel: String?
                    get() = "Action"
                override val duration: SnackbarDuration
                    get() = SnackbarDuration.Short
                override val message: String = "Test Error"
                override val withDismissAction: Boolean
                    get() = false
            }

        override fun dismiss() {
        }

        override fun performAction() {
        }

    }

    Column {
        AppTheme {
            ErrorSnackbar(snackbarData = snackbarData)
        }
        AppTheme(darkTheme = true) {
            ErrorSnackbar(snackbarData = snackbarData)
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview
@Composable
private fun AddFromWebPreview() {
    var loading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    AppTheme(/*darkTheme = true*/) {
        Scaffold {
            AddFromWeb(
                modifier = Modifier.padding(it),
                url = "https://www.marmiton.org/recettes/recette_la-vraie-tartiflette_17634.aspx",
                loading = loading,
                navigateBack = {},
                onExtract = {
                    loading = true
                    scope.launch {
                        delay(2000)
                        loading = false
                    }
                })
        }
    }
}

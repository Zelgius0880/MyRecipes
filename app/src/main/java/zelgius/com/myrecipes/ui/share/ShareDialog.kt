package zelgius.com.myrecipes.ui.share

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.GenerateQrCodeUseCase
import zelgius.com.myrecipes.data.useCase.pdf.GeneratePdfUseCase
import zelgius.com.myrecipes.ui.preview.createDummyModel
import java.io.OutputStream
import javax.inject.Inject

@Composable
fun ShareDialog(
    recipe: Recipe,
    viewModel: ShareDialogViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {

    val width = with(LocalDensity.current) { 500.dp.toPx() }.toInt()
    val height = with(LocalDensity.current) { 500.dp.toPx() }.toInt()
    val color = Color.Black.toArgb()

    var progress by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(recipe) {
        viewModel.generateQrCode(recipe, width = width, height = height, color = color)
    }

    val qrCode by viewModel.qrCode.collectAsStateWithLifecycle(null)
    val context = LocalContext.current

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) {

            coroutineScope.launch {
                if (it != null) {
                    val outputStream: OutputStream = context.contentResolver?.openOutputStream(it)
                        ?: return@launch
                    viewModel.generatePdf(recipe, outputStream)
                    outputStream.close()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(it, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "${context.getString(R.string.open)} - ${recipe.name}"
                        )
                    )
                }
            }
            progress = false
        }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.White)
                ) {
                    qrCode?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = recipe.name,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                if (progress)
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                else
                    Button(onClick = {
                        progress = true

                        saveFileLauncher.launch("${recipe.name}.pdf")
                    }, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.export_to_pdf))
                    }
            }
        }
    }
}

@HiltViewModel
class ShareDialogViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val generatePdfUseCase: GeneratePdfUseCase
) : ViewModel() {

    private val _qrCode = MutableStateFlow<Bitmap?>(null)
    val qrCode = _qrCode.filterNotNull()
    private val generateQrCodeUseCase = GenerateQrCodeUseCase()

    fun generateQrCode(recipe: Recipe, @ColorInt color: Int, width: Int = 400, height: Int = 400) {
        viewModelScope.launch {
            _qrCode.value = generateQrCodeUseCase.execute(
                recipeRepository.getBytes(recipe),
                width = width,
                height = height,
                dotColor = color
            )
        }
    }

    suspend fun generatePdf(recipe: Recipe, outputStream: OutputStream) =
        generatePdfUseCase.execute(recipe, outputStream)

}

@Preview
@Composable
fun ShareDialogPreview() {
    val recipe = createDummyModel()
    ShareDialog(recipe = recipe, onDismiss = { })
}
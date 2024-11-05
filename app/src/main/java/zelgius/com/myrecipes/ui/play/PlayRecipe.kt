package zelgius.com.myrecipes.ui.play

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zelgius.com.myrecipes.BuildConfig
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.data.useCase.instructions
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.play.viewModel.PlayRecipeViewModel
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.utils.rememberIsInPipMode

// Constant for broadcast receiver
const val ACTION_BROADCAST_CONTROL = "broadcast_control"

// Intent extras for broadcast controls from Picture-in-Picture mode.
const val EXTRA_CONTROL_TYPE = "control_type"
const val EXTRA_CONTROL_NEXT = 1
const val EXTRA_CONTROL_PREVIOUS = 2

@Composable
fun PlayRecipe(
    modifier: Modifier = Modifier,
    viewModel: PlayRecipeViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val instructions by viewModel.instructions.collectAsStateWithLifecycle()
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val isTextReadingChecked by viewModel.readingEnabled.collectAsStateWithLifecycle(true)
    val isGestureRecognitionChecked by viewModel.gestureRecognitionEnabled.collectAsStateWithLifecycle(true)
    val isGestureRecognitionError by viewModel.gestureRecognitionError.collectAsStateWithLifecycle(false)
    val isInPipMode = rememberIsInPipMode()
    val currentItemPosition by viewModel.currentInstructionPosition.collectAsStateWithLifecycle()

    val root = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val showPreview = isGestureRecognitionChecked&& BuildConfig.DEBUG

    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && isGestureRecognitionChecked) {
            viewModel.startGestureRecognition(lifecycleOwner, if(showPreview) previewView else null, root.display.rotation)
        }
    }


    LaunchedEffect(isGestureRecognitionChecked) {
        if(!isGestureRecognitionChecked ) {
            viewModel.cancelRecognition()
            return@LaunchedEffect
        }

        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED){
            launcher.launch(android.Manifest.permission.CAMERA)
        } else {
            viewModel.startGestureRecognition(lifecycleOwner,  if(showPreview) previewView else null, root.display.rotation)
        }
    }

    recipe?.let {
        Box {

            PlayRecipe(
                recipe = it,
                instructions = instructions,
                isTextReadingChecked = isTextReadingChecked,
                isGestureRecognitionChecked = isGestureRecognitionChecked && !isGestureRecognitionError,
                isGestureRecognitionError = isGestureRecognitionError ,
                isPipMode = isInPipMode,
                modifier = modifier,
                currentItemPosition = currentItemPosition,
                onIndexChanged = {
                    viewModel.onInstructionSelected(it)
                },
                onNext = {
                    viewModel.onNext()
                },
                onPrevious = {
                    viewModel.onPreview()
                },
                onTextReadingChecked = {
                    viewModel.onReadingEnabled(it)
                },
                onGestureRecognitionChecked = {
                    viewModel.onGestureRecognitionEnabled(it)
                },
                onBack = onBack,
            )

            if(showPreview)
                AndroidView(factory = { previewView }, modifier = Modifier.size(64.dp).align(Alignment.TopEnd))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayRecipe(
    recipe: Recipe,
    instructions: List<InstructionItem>,
    modifier: Modifier = Modifier,
    currentItemPosition: Int = 0,
    isTextReadingChecked: Boolean = false,
    isGestureRecognitionChecked: Boolean = false,
    isGestureRecognitionError: Boolean = false,
    isPipMode: Boolean = false,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onIndexChanged: (index: Int) -> Unit = {},
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
    onGestureRecognitionChecked: (checked: Boolean) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(isPipMode, currentItemPosition) {
        if (!isPipMode) lazyListState.animateScrollToItem(currentItemPosition, -256)
    }

    if (isPipMode) {
        PipView(
            recipe,
            lazyListState = lazyListState,
            currentItemPosition = currentItemPosition,
            instructions = instructions,
            modifier = modifier,
            onNext = onNext,
            onPrevious = onPrevious
        )
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    })
            }
        ) {
            Column(modifier = Modifier.padding(it)) {
                RecipeHeader(
                    recipe,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    isTextReadingChecked = isTextReadingChecked,
                    onTextReadingChecked = onTextReadingChecked,
                    isGestureDetectionChecked = isGestureRecognitionChecked,
                    isGestureDetectionError = isGestureRecognitionError,
                    onGestureDetectionChecked = onGestureRecognitionChecked,
                )

                ButtonRow(
                    onPrevious = onPrevious,
                    onNext = onNext
                )

                InstructionList(
                    lazyListState = lazyListState,
                    instructions = instructions,
                    modifier = Modifier
                        .weight(2f)
                        .padding(bottom = 8.dp),
                    currentItemPosition = currentItemPosition
                ) { index ->
                    onIndexChanged(index)
                }

            }
        }
    }
}

@Composable
private fun ButtonRow(
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    Row(modifier = Modifier.padding(16.dp)) {
        OutlinedButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNext) {
            Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
        }
    }
}


@Composable
@Preview(device = Devices.PIXEL_7_PRO, showSystemUi = true)
fun PlayRecipePreview() {
    val recipe = createDummyModel()
    val instructions = recipe.instructions

    AppTheme {
        PlayRecipe(recipe, instructions)
    }
}
@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.gestureSetUp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.ui.AppTheme
import kotlin.math.roundToInt

@Composable
fun GestureSetUpScreen(viewModel: GestureSetUpViewModel = hiltViewModel(), onBack: () -> Unit) {

    val root = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    val previewView = remember {
        PreviewView(context)
    }

    var height by remember {
        mutableFloatStateOf(0f)
    }

    var width by remember {
        mutableFloatStateOf(0f)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startDetection(
                owner = lifecycleOwner,
                previewView = previewView,
                orientation = root.display.rotation,
                width = width.toDouble(),
                height = height.toDouble()
            )
        } else onBack()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(android.Manifest.permission.CAMERA)
        } else {
            viewModel.startDetection(
                owner = lifecycleOwner,
                previewView = previewView,
                orientation = root.display.rotation,
                width = width.toDouble(),
                height = height.toDouble()
            )
        }
    }

    val gestureDetected by viewModel.gestureDetected.collectAsStateWithLifecycle(false)
    val landmarks by viewModel.lastLandmarksDetected.collectAsStateWithLifecycle()

    Box(modifier = Modifier.onGloballyPositioned { coordinates ->
        // Set column height using the LayoutCoordinates
        height = coordinates.size.height.toFloat()
        width = coordinates.size.width.toFloat()
    }) {

        val color1 by rememberInfiniteTransition().animateColor(
            initialValue = MaterialTheme.colorScheme.secondary,
            targetValue = MaterialTheme.colorScheme.primary,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color1"
        )
        val color2 by rememberInfiniteTransition().animateColor(
            initialValue = MaterialTheme.colorScheme.primary,
            targetValue = MaterialTheme.colorScheme.secondary,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color2"
        )


        AndroidView(
            factory = { previewView }, modifier = Modifier
                .fillMaxSize()
        )

        LandmarksOverlay(landmarks = landmarks, modifier = Modifier.fillMaxSize())

        val area by viewModel.gestureDetectionArea.collectAsStateWithLifecycle(0f)

        SensibilitySlider(
            area,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            onValueChange = viewModel::submitGestureDetectionArea
        )


        AnimatedVisibility(
            gestureDetected, exit = scaleOut(), enter = scaleIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp),
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(color1, color2)))
                    .padding(8.dp)
            )
        }
    }

}

@Composable
private fun SensibilitySlider(
    area: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit = {},
) {
    var value by remember {
        mutableFloatStateOf(area)
    }

    LaunchedEffect(area) {
        value = area
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(48.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${stringResource(R.string.gesture_sensibility_title)}: ${(value * 100).roundToInt()}%",
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Text(
                stringResource(R.string.gesture_sensibility_far_label),
                style = MaterialTheme.typography.labelSmall,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                stringResource(R.string.gesture_sensibility_close_label),
                style = MaterialTheme.typography.labelSmall
            )
        }

        val interactionSource = remember { MutableInteractionSource() }

        Slider(
            modifier = Modifier
                .offset(y = -(8.dp))
                .padding(horizontal = 8.dp),
            value = value,
            interactionSource = interactionSource,
            onValueChange = {
                value = it
            },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(),
                    thumbSize = DpSize(6.dp, 24.dp)
                )
            },
            onValueChangeFinished = { onValueChange(value) },
            valueRange = 0f..0.75f,
        )
    }
}

@Preview
@Composable
fun SensibilitySliderPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            SensibilitySlider(modifier = Modifier.padding(8.dp), area = 0.2f) {}
        }
    }
}
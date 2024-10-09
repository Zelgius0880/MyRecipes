package zelgius.com.myrecipes.ui.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.useCase.IngredientInstruction
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.data.useCase.StepInstruction
import zelgius.com.myrecipes.data.useCase.instructions
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.LocalStepCardValues
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.Step
import zelgius.com.myrecipes.ui.play.viewModel.PlayRecipeViewModel
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.utils.rememberIsInPipMode
import kotlin.math.absoluteValue

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
    val isInPipMode = rememberIsInPipMode()

    recipe?.let {
        PlayRecipe(
            recipe = it,
            instructions = instructions,
            isTextReadingChecked = isTextReadingChecked,
            isPipMode = isInPipMode,
            modifier = modifier,
            onInstructionSelected = {
                viewModel.onInstructionSelected(it)
            },
            onTextReadingChecked = {
                viewModel.onReadingEnabled(it)
            },
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayRecipe(
    recipe: Recipe,
    instructions: List<InstructionItem>,
    modifier: Modifier = Modifier,
    isTextReadingChecked: Boolean = false,
    isPipMode: Boolean = false,
    onInstructionSelected: (InstructionItem) -> Unit = {},
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentItemPosition by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isPipMode) {
        if(!isPipMode) {
            lazyListState.animateScrollToItem(currentItemPosition, -256)
        }
    }

    fun next(): Boolean {
        if (currentItemPosition < instructions.lastIndex) {
            ++currentItemPosition
            onInstructionSelected(instructions[currentItemPosition])

            if (!isPipMode)
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(currentItemPosition, -256)
                }
            return true
        }

        return false
    }

    fun previous(): Boolean {
        if (currentItemPosition >= 1) {
            --currentItemPosition
            onInstructionSelected(instructions[currentItemPosition])

            if (!isPipMode)
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(currentItemPosition, -256)
                }

            return true
        }

        return false
    }

    if (isPipMode) {
        PipView(recipe, instructions[currentItemPosition], modifier, onNext = {
            next()
        }, onPrevious = {
            previous()
        })
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
                    isTextReadingChecked,
                    onTextReadingChecked
                )

                ButtonRow(
                    onPrevious = { previous() },
                    onNext = { next() }
                )

                InstructionList(
                    lazyListState,
                    instructions,
                    currentItemPosition
                ) { index, instruction ->
                    currentItemPosition = index
                    onInstructionSelected(instruction)
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
private fun PipView(
    recipe: Recipe,
    item: InstructionItem,
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    val context = LocalContext.current

    DisposableEffect(recipe) {
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if ((intent == null) || (intent.action != ACTION_BROADCAST_CONTROL)) {
                    return
                }

                when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                    EXTRA_CONTROL_NEXT -> onNext()
                    EXTRA_CONTROL_PREVIOUS -> onPrevious()
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            IntentFilter(ACTION_BROADCAST_CONTROL),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(recipe.name, style = MaterialTheme.typography.titleMedium
            .copy(color = MaterialTheme.colorScheme.onSurface ))

        CompositionLocalProvider(LocalTextStyle provides
                MaterialTheme.typography.bodyLarge
                    .copy(color = MaterialTheme.colorScheme.onSurface )) {
            when (item) {
                is IngredientInstruction -> Ingredient(
                    item.ingredient,
                    modifier = Modifier.weight(1f)
                )

                is StepInstruction -> Step(
                    item.step,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.InstructionList(
    lazyListState: LazyListState,
    instructions: List<InstructionItem>,
    currentItemPosition: Int,
    onInstructionSelected: (index: Int, InstructionItem) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .weight(2f)
            .padding(bottom = 8.dp),
    ) {
        itemsIndexed(instructions) { index, item ->
            val ratio by animateFloatAsState(
                if (currentItemPosition == index) 1f else (1f / ((currentItemPosition - index).absoluteValue + 1)) * 1.6f,
                label = "ration"
            )

            val modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .alpha(ratio)
                .graphicsLayer(
                    ratio,
                    ratio,
                    transformOrigin = TransformOrigin(0f, 0.5f)
                )
                .fillMaxWidth()

            val cardColor by animateColorAsState(
                if (currentItemPosition == index) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.background
            )

            Card(
                shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50),
                modifier = Modifier.padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                onClick = {
                    onInstructionSelected(index, item)
                }
            ) {
                CompositionLocalProvider(LocalTextStyle provides TextStyle(fontSize = 36.sp)) {
                    CompositionLocalProvider(
                        LocalStepCardValues provides LocalStepCardValues.current.copy(
                            iconSize = 56.dp,
                            iconPadding = 8.dp,
                        )
                    ) {

                        when (item) {
                            is IngredientInstruction -> Ingredient(
                                item.ingredient,
                                modifier
                            )

                            is StepInstruction -> Step(item.step, modifier)
                        }
                    }
                }
            }

        }

        item {
            Box(modifier = Modifier.height(512.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeHeader(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    isTextReadingChecked: Boolean = false,
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
) {
    Card(
        modifier = modifier
            .padding(top = 8.dp), shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = rememberAsyncImagePainter(
                    recipe.imageUrl, error = painterResource(R.drawable.ic_dish)
                ), contentDescription = null, modifier = Modifier
                    .size(128.dp)
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge
                    ), contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    recipe.name, modifier = Modifier
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineLarge
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.TwoTone.RecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isTextReadingChecked,
                        onCheckedChange = onTextReadingChecked
                    )
                }
            }

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
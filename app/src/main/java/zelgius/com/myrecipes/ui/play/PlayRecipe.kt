package zelgius.com.myrecipes.ui.play

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zelgius.com.myrecipes.data.useCase.IngredientInstruction
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.data.useCase.StepInstruction
import zelgius.com.myrecipes.data.useCase.instructions
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.Step
import zelgius.com.myrecipes.ui.play.viewModel.PlayRecipeViewModel
import zelgius.com.myrecipes.ui.preview.createDummyModel
import kotlin.math.absoluteValue

@Composable
fun PlayRecipe(viewModel: PlayRecipeViewModel = hiltViewModel(), id: Long) {
    viewModel.load(id)
    val instructions by viewModel.instructions.collectAsStateWithLifecycle()
    PlayRecipe(instructions)
}

@Composable
fun PlayRecipe(instructions: List<InstructionItem>) {
    var currentItemPosition by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()
    Column {

        LazyColumn(state = lazyListState, modifier = Modifier.padding(8.dp)) {
            itemsIndexed(instructions) { index, item ->
                val ratio by animateFloatAsState(
                    if (currentItemPosition == index) 1f else (1f / ((currentItemPosition - index).absoluteValue + 1))*1.6f,
                    label = "ration"
                )

                val modifier = Modifier
                    .alpha(ratio)
                    .graphicsLayer(ratio, ratio, transformOrigin = TransformOrigin(0f,0.5f))
                    .fillMaxWidth()


                when (item) {
                    is IngredientInstruction -> Ingredient(item.ingredient, modifier)
                    is StepInstruction -> Step(item.step, modifier)
                }

            }
        }

        Button(onClick = {
            currentItemPosition = (currentItemPosition + 1) % instructions.size
        }) {
            Text("Next")
        }
    }
}

@Composable
@Preview(device = Devices.PIXEL_7_PRO, showSystemUi = true)
fun PlayRecipePreview() {
    val instructions = createDummyModel().instructions

    MaterialTheme {
        PlayRecipe(instructions)
    }
}
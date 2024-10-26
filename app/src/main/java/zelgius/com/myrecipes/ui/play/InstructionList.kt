package zelgius.com.myrecipes.ui.play

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zelgius.com.myrecipes.data.useCase.IngredientInstruction
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.data.useCase.StepInstruction
import zelgius.com.myrecipes.ui.common.LocalStepCardValues
import zelgius.com.myrecipes.ui.common.StepCardValues
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.Step
import zelgius.com.myrecipes.utils.ifNotNull
import kotlin.math.absoluteValue


@Composable
fun InstructionList(
    lazyListState: LazyListState,
    instructions: List<InstructionItem>,
    currentItemPosition: Int,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 28.sp),
    maxHeight: Dp? = null,
    stepCardValues: StepCardValues = LocalStepCardValues.current.copy(
        iconSize = 56.dp,
        iconPadding = 8.dp,
    ),
    onInstructionSelected: (index: Int) -> Unit = { }
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
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
                shape = MaterialTheme.shapes.extraLarge.copy(
                    topStart = ZeroCornerSize,
                    bottomStart = ZeroCornerSize
                ),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .ifNotNull(maxHeight) {
                        Modifier.heightIn(max = it)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = cardColor,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                onClick = {
                    onInstructionSelected(index)
                }
            ) {
                CompositionLocalProvider(LocalTextStyle provides textStyle) {
                    CompositionLocalProvider(
                        LocalStepCardValues provides stepCardValues
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

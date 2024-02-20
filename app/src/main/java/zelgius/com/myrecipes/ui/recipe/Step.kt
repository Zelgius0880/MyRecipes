package zelgius.com.myrecipes.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.md_cyan_A700

@Composable
fun Step(step: Step) {
    Card {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StepContent(step = step)
        }
    }
}

@Composable
private fun StepContent(step: Step) {

    Row {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(md_cyan_A700), contentAlignment = Alignment.Center
        ) {
            Text("${step.order}")
        }

        Text(text = step.text, Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
@Preview
fun StepPreview() {
    AppTheme {
        Step(step = Step(
            text = "Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Text",
            order = 1,
            recipe = Recipe(name = "Recipe", type = Recipe.Type.Other)
        ))
    }
}
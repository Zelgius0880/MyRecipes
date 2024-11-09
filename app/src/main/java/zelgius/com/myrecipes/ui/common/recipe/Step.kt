package zelgius.com.myrecipes.ui.common.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.LocalStepCardValues
import zelgius.com.myrecipes.ui.common.autosizetext.AutoSizeText
import zelgius.com.myrecipes.utils.conditional

@Composable
fun Step(step: Step, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = CenterVertically) {
        StepContent(step = step)
    }
}


@Composable
private fun StepContent(step: Step) {
    val values = LocalStepCardValues.current
    val textStyle = LocalTextStyle.current

    Row(modifier = Modifier.conditional(step.optional){alpha(0.6f)}) {
        Box(
            Modifier
                .size(values.iconSize)
                .clip(CircleShape)
                .background(values.avatarColor), contentAlignment = Alignment.Center
        ) {
            Text("${step.order}", style = textStyle)
        }

        val modifier = Modifier.padding(horizontal = 8.dp)
        if (values.autoResize)
            AutoSizeText(
                text = step.text,
                maxTextSize = textStyle.fontSize,
                minTextSize = 10.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier,
                style = textStyle
            )
        else
            Text(text = step.text, modifier, style = textStyle)
    }
}

@Composable
fun IngredientChip(count: Int) {
    Badge(
        modifier = Modifier.padding(end = 8.dp, top = 8.dp),
        containerColor = MaterialTheme.colorScheme.tertiary,
    ) {
        Row(
            verticalAlignment = CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                    .size(16.dp),
                painter = painterResource(R.drawable.ic_carrot_solid),
                contentDescription = null
            )
            Text(
                text = "$count",
            )
        }
    }
}

@Composable
@Preview
fun StepPreview() {
    AppTheme {
        Step(
            step = Step(
                text = "Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Long Text",
                order = 1,
                recipe = Recipe(name = "Recipe", type = Recipe.Type.Other)
            )
        )
    }
}
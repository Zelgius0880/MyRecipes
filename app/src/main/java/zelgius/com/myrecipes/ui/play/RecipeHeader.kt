package zelgius.com.myrecipes.ui.play

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.RecordVoiceOver
import androidx.compose.material.icons.twotone.SignLanguage
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.common.IconSwitch
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.utils.isTwoPanes


@Composable
fun RecipeHeader(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    isTextReadingChecked: Boolean = false,
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
    isGestureDetectionChecked: Boolean = false,
    isGestureDetectionError: Boolean = false,
    onGestureDetectionChecked: (checked: Boolean) -> Unit = {},
) {
    if (isTwoPanes()) RecipeHeaderTwoPanes(
        recipe,
        modifier,
        isTextReadingChecked,
        onTextReadingChecked,
        isGestureDetectionChecked,
        isGestureDetectionError,
        onGestureDetectionChecked
    ) else RecipeHeaderOnePane(
        recipe,
        modifier,
        isTextReadingChecked,
        onTextReadingChecked,
        isGestureDetectionChecked,
        isGestureDetectionError,
        onGestureDetectionChecked
    )
}

@Composable
private fun MainCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    additionalContent: @Composable ColumnScope.() -> Unit = {}
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

                additionalContent()
            }

        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeHeaderOnePane(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    isTextReadingChecked: Boolean = false,
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
    isGestureDetectionChecked: Boolean = false,
    isGestureDetectionError: Boolean = false,
    onGestureDetectionChecked: (checked: Boolean) -> Unit = {},
) {
    MainCard(
        recipe,
        modifier
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.End)
        ) {

            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                    color = if (isGestureDetectionError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            ) {
                GestureDetectionSwitch(
                    isGestureDetectionChecked, onGestureDetectionChecked, Modifier
                        .padding(vertical = 4.dp)
                )
            }
            TextReadingSwitch(
                isTextReadingChecked, onTextReadingChecked, Modifier
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun RecipeHeaderTwoPanes(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    isTextReadingChecked: Boolean = false,
    onTextReadingChecked: (checked: Boolean) -> Unit = {},
    isGestureDetectionChecked: Boolean = false,
    isGestureDetectionError: Boolean = false,
    onGestureDetectionChecked: (checked: Boolean) -> Unit = {},
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        MainCard(
            recipe,
            modifier
        )

        Spacer(modifier.weight(1f))

        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                CompositionLocalProvider(
                    LocalContentColor provides if (isGestureDetectionError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                ) {
                    GestureDetectionSwitch(
                        isGestureDetectionChecked, onGestureDetectionChecked, Modifier
                            .padding(vertical = 4.dp, horizontal = 16.dp)
                    )
                }
                TextReadingSwitch(
                    isTextReadingChecked, onTextReadingChecked, Modifier
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                )
            }
        }
    }

}

@Composable
private fun GestureDetectionSwitch(
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconSwitch(
        checked = checked,
        onCheckedChange = onCheckChanged,
        tooltip = stringResource(R.string.play_recipe_enable_gesture_detection),
        modifier = modifier,
        icon = {
            Icon(
                Icons.TwoTone.SignLanguage,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    )
}

@Composable
private fun TextReadingSwitch(
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconSwitch(
        checked = checked,
        onCheckedChange = onCheckChanged,
        tooltip = stringResource(R.string.play_recipe_enable_text_reading),
        modifier = modifier,
        icon = {
            Icon(
                Icons.TwoTone.RecordVoiceOver,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    )
}

@Preview(device = Devices.PIXEL_5)
@Composable
fun RecipeHeaderOnPanePreview() {
    RecipeHeaderOnePane(createDummyModel())
}


@Preview(device = Devices.PIXEL_TABLET)
@Composable
fun RecipeHeaderTwoPanesPreview() {
    RecipeHeaderTwoPanes(createDummyModel())
}


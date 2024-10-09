package zelgius.com.myrecipes.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import zelgius.com.myrecipes.ui.md_blue_grey_700

data class StepCardValues(
    val iconSize: Dp,
    val iconPadding: Dp,
    val avatarColor: Color,
)

val LocalStepCardValues = compositionLocalOf {
    StepCardValues(
        iconSize = 36.dp,
        iconPadding = 8.dp,
        avatarColor = md_blue_grey_700,
    )
}

@Composable
fun StepCard(
    letter: String,
    text: String,
    modifier: Modifier = Modifier,
    ) {
    StepCard(
        avatar = Avatar.Letter(letter),
        text = text,
        modifier = modifier,
    )
}

@Composable
fun StepCard(
    @DrawableRes image: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    StepCard(
        avatar = Avatar.Image(image),
        text = text,
        modifier = modifier,
    )
}


@Composable
private fun StepCard(
    avatar: Avatar,
    text: String,
    modifier: Modifier
) {
    val values = LocalStepCardValues.current
    val textStyle = LocalTextStyle.current

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(values.iconSize)
                .clip(CircleShape)
                .background(values.avatarColor), contentAlignment = Alignment.Center
        ) {

            when (avatar) {
                is Avatar.Image -> Image(
                    painter = painterResource(id = avatar.image),
                    modifier = Modifier.padding(all = values.iconPadding),
                    contentDescription = null
                )

                is Avatar.Letter -> Text(avatar.letter, style = textStyle)
            }
        }

        Text(text = text, Modifier.padding(horizontal = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
    }
}

@Preview
@Composable
fun StepCardPreview() {
    StepCard(
        letter = "A",
        text = " Bla sdlkjfls",
    )
}

private sealed interface Avatar {
    data class Letter(val letter: String) : Avatar
    data class Image(@DrawableRes val image: Int) : Avatar
}
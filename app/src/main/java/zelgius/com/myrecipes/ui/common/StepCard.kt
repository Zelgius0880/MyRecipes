package zelgius.com.myrecipes.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zelgius.com.myrecipes.ui.md_blue_grey_700

@Composable
fun StepCard(
    letter: String,
    text: String,
    avatarColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,

    ) {
    StepCard(
        avatar = Avatar.Letter(letter),
        avatarColor = avatarColor,
        text = text,
        modifier = modifier,
        shape = shape
    )
}

@Composable
fun StepCard(
    @DrawableRes image: Int,
    text: String,
    avatarColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    StepCard(
        avatar = Avatar.Image(image),
        avatarColor = avatarColor,
        text = text,
        modifier = modifier,
        shape = shape
    )
}


@Composable
private fun StepCard(
    avatar: Avatar,
    text: String,
    avatarColor: Color,
    shape: Shape,
    modifier: Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(avatarColor), contentAlignment = Alignment.Center
        ) {

            when (avatar) {
                is Avatar.Image -> Image(
                    painter = painterResource(id = avatar.image),
                    modifier = Modifier.padding(all = 8.dp),
                    contentDescription = null
                )

                is Avatar.Letter -> Text(avatar.letter)
            }
        }

        Text(text = text, Modifier.padding(horizontal = 8.dp))
    }
}

@Preview
@Composable
fun StepCardPreview() {
    StepCard(
        shape = RoundedCornerShape(0.dp),
        letter = "A",
        text = " Bla sdlkjfls",
        avatarColor = md_blue_grey_700
    )
}

private sealed interface Avatar {
    data class Letter(val letter: String) : Avatar
    data class Image(@DrawableRes val image: Int) : Avatar
}
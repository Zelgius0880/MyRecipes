package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import zelgius.com.myrecipes.R

@Composable
fun AppImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var isError by remember { mutableStateOf(imageUrl == null) }

    LaunchedEffect(imageUrl) {
        isError = imageUrl == null
    }

    if (!isError)
        AsyncImage(
            model = imageUrl,
            onError = { isError = true },
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    else Image(
        painterResource(R.drawable.ic_dish), contentDescription = contentDescription,
        modifier = modifier,
    )
}
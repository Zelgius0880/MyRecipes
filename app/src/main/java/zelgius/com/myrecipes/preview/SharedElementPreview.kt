@file:OptIn(ExperimentalSharedTransitionApi::class)

package zelgius.com.myrecipes.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SharedElementPreview(content: @Composable (animatedScope: AnimatedContentScope, sharedTransitionScope: SharedTransitionScope) -> Unit) {
    MaterialTheme {
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "preview") {
                if (it) content(this@AnimatedContent, this@SharedTransitionLayout)
            }
        }
    }
}
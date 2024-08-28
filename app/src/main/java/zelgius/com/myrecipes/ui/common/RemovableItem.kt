package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun RemovableItem(modifier: Modifier = Modifier, onRemove: () -> Unit, content: @Composable () -> Unit, ) {
    AppSwipeRevealItem(
        contentElevation = 2.dp,
        modifier = modifier,
        endActionSize = 64.dp,
        endAction = {
            Button(
                modifier = Modifier
                    .fillMaxSize(),
                onClick = {
                    onRemove()
                },
                shape = RectangleShape,
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.TwoTone.Delete,
                    contentDescription = "",
                )
            }
        }) {
        Surface(
            color = CardDefaults.cardColors().containerColor,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            content()
        }
    }
}
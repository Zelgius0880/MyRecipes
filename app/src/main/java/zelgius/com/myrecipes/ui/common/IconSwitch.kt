package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String? = null,
) {
    @Composable
    fun Content() {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }

    if (tooltip != null)
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip(caretSize = TooltipDefaults.caretSize) { Text(tooltip) } },
            state = rememberTooltipState()
        ) {
            Content()
        }
    else Content()
}
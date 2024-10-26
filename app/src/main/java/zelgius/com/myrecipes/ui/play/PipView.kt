package zelgius.com.myrecipes.ui.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.ui.common.LocalStepCardValues

@Composable
fun PipView(
    recipe: Recipe,
    onNext: () -> Unit,
    lazyListState: LazyListState,
    instructions: List<InstructionItem>,
    currentItemPosition: Int,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(currentItemPosition) {
        lazyListState.animateScrollToItem(currentItemPosition, 0)
    }

    DisposableEffect(recipe) {
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if ((intent == null) || (intent.action != ACTION_BROADCAST_CONTROL)) {
                    return
                }

                when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                    EXTRA_CONTROL_NEXT -> onNext()
                    EXTRA_CONTROL_PREVIOUS -> onPrevious()
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            IntentFilter(ACTION_BROADCAST_CONTROL),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            recipe.name, style = MaterialTheme.typography.titleMedium
                .copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        BoxWithConstraints {
            val boxWithConstraintsScope = this

            InstructionList(
                instructions = instructions,
                currentItemPosition = currentItemPosition,
                lazyListState = lazyListState,
                maxHeight = boxWithConstraintsScope.maxHeight,
                textStyle = MaterialTheme.typography.bodyMedium
                    .copy(color = MaterialTheme.colorScheme.onSurface),
                stepCardValues = LocalStepCardValues.current.copy(
                    iconSize = 32.dp,
                    iconPadding = 4.dp,
                    autoResize = true
                )
            )
        }
    }
}
package zelgius.com.myrecipes

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.play.ACTION_BROADCAST_CONTROL
import zelgius.com.myrecipes.ui.play.EXTRA_CONTROL_NEXT
import zelgius.com.myrecipes.ui.play.EXTRA_CONTROL_PREVIOUS
import zelgius.com.myrecipes.ui.play.EXTRA_CONTROL_TYPE
import zelgius.com.myrecipes.ui.play.PlayRecipe
import zelgius.com.myrecipes.ui.play.viewModel.PlayRecipeViewModel
import zelgius.com.myrecipes.utils.findActivity
import kotlin.getValue

@AndroidEntryPoint
class PlayRecipeActivity : ComponentActivity() {
    companion object {
        const val RECIPE_ID_PARAM = "RECIPE_ID_PARAM"

        fun start(context: Context, recipeId: Long) {
            context.startActivity(Intent(context, PlayRecipeActivity::class.java).apply {
                putExtra(RECIPE_ID_PARAM, recipeId)
            })
        }
    }

    val viewModel: PlayRecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val pipModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                val builder = PictureInPictureParams.Builder()
                    .setActions(
                        listOf(
                            RemoteAction(
                                Icon.createWithResource(
                                    context,
                                    R.drawable.round_arrow_back_24
                                ),
                                getString(R.string.previous),
                                getString(R.string.previous),
                                PendingIntent.getBroadcast(
                                    context,
                                    EXTRA_CONTROL_PREVIOUS,
                                    Intent().apply {
                                        action = ACTION_BROADCAST_CONTROL
                                        putExtra(EXTRA_CONTROL_TYPE, EXTRA_CONTROL_PREVIOUS)
                                        `package` = context.packageName
                                    },
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            ),
                            RemoteAction(
                                Icon.createWithResource(
                                    context,
                                    R.drawable.round_arrow_forward_24
                                ),
                                getString(R.string.next),
                                getString(R.string.next),
                                PendingIntent.getBroadcast(
                                    context,
                                    EXTRA_CONTROL_NEXT,
                                    Intent().apply {
                                        action = ACTION_BROADCAST_CONTROL
                                        putExtra(EXTRA_CONTROL_TYPE, EXTRA_CONTROL_NEXT)
                                        `package` = context.packageName
                                    },
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            ),

                            )
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(true)
                }
                context.findActivity().setPictureInPictureParams(builder.build())
            }

            val recipeId = intent.getLongExtra(RECIPE_ID_PARAM, -1)
            if (recipeId > -1) {
                viewModel.load(recipeId)

                AppTheme {
                    PlayRecipe(viewModel = viewModel, modifier = pipModifier, onBack = {
                        finish()
                    })
                }
            }
        }
    }

}
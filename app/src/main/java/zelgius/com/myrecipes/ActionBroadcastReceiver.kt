package zelgius.com.myrecipes

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import zelgius.com.myrecipes.entities.IngredientForRecipe
import zelgius.com.myrecipes.entities.Step
import zelgius.com.myrecipes.utils.UiUtils

class ActionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val list: Array<Parcelable> = intent.getParcelableArrayExtra("LIST")?: arrayOf()
        val index = intent.getIntExtra("INDEX", -1) + 1
        val title = intent.getStringExtra("TITLE")

        if(index > 0 && index < list.size) {
//This is the intent of PendingIntent
            val intentAction = Intent(context, ActionBroadcastReceiver::class.java)

            //This is optional if you have more than one buttons and want to differentiate between two
            intentAction.putExtra("LIST", list)
            intentAction.putExtra("INDEX", index)
            intentAction.putExtra("TITLE", title)
            val o = list[index]
            val text = when (o) {
                is Step -> o.text
                is IngredientForRecipe -> IngredientForRecipe.text(context, o)
                else -> error("Should not be there")
            }.let {
                if(o is Step && o.optional || o is IngredientForRecipe && (o.optional == true || o.step?.optional == true))
                    "($it)"
                else it
            }

            val drawable = when (o) {
                is Step -> UiUtils.getDrawable(
                    context,
                    "${o.order}"
                )
                is IngredientForRecipe -> UiUtils.getDrawable(
                    context,
                    if(!o.imageUrl.isNullOrEmpty()) o.imageUrl!! else o.name
                )
                else -> error("Should not be there")
            }

            val pIntent =
                PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(context, "recipe")
                .setSmallIcon(R.drawable.ic_restaurant_menu_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setVibrate(longArrayOf(0L))
                .setSound(null)
                .setLargeIcon(drawable!!.toBitmap())
                .setContentIntent(
                    PendingIntent.getActivity(context, 0,
                        Intent(context, MainActivity::class.java ).apply {
                            putExtras(bundleOf("ID" to intent.getLongExtra("ID_FROM_NOTIF", 0L)))
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )

            if(index < list.size -1)
                builder.addAction(
                    R.drawable.ic_skip_next_black_24dp,
                    context.getString(R.string.next),
                    pIntent
                )

            builder.priority =
                NotificationCompat.PRIORITY_DEFAULT

            NotificationManagerCompat.from(context).notify(5, builder.build())
        }

    }

}
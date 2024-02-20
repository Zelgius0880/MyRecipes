package zelgius.com.myrecipes

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import zelgius.com.myrecipes.dialogs.IntroDialog
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.home.Home
import zelgius.com.myrecipes.ui.home.HomeViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) processIntent(intent)

        setContent {
            AppTheme {
                Home()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        if (intent.hasExtra("ID_FROM_NOTIF")) {
            /*viewModel.loadRecipe(intent.getLongExtra("ID_FROM_NOTIF", 0L)).observe(this) {
                if (it != null) {
                    if (navController.currentDestination?.id != R.id.tabFragment)
                        navController.navigate(R.id.tabFragment)

                    navController.navigate(
                        R.id.action_tabFragment_to_recipeFragment,
                        bundleOf("RECIPE" to it),
                        null,
                        null
                    )

                    viewModel.loadRecipe(it.id!!)
                }
            }
*/
        }
    }




    override fun onResume() {
        super.onResume()

        if (getSharedPreferences("DEFAULT", MODE_PRIVATE).getBoolean("SHOW_POP", true)) {
            IntroDialog().show(supportFragmentManager, "intro")
        }
    }
}
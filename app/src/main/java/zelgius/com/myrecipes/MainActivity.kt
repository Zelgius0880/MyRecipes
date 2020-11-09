package zelgius.com.myrecipes

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import zelgius.com.myrecipes.dialogs.IntroDialog

class MainActivity : AppCompatActivity() {


    private val navController
        get() = findNavController(this, R.id.nav_host_fragment)

    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (intent != null) processIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        if (intent.hasExtra("ID_FROM_NOTIF")) {
            viewModel.loadRecipe(intent.getLongExtra("ID_FROM_NOTIF", 0L)).observe(this) {
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

        }
    }

    override fun onSupportNavigateUp(): Boolean {

        return navController.navigateUp()
    }


    override fun onResume() {
        super.onResume()

        if (getSharedPreferences("DEFAULT", MODE_PRIVATE).getBoolean("SHOW_POP", true)) {
            IntroDialog().show(supportFragmentManager, "intro")
        }
    }
}
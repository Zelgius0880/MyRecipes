package zelgius.com.myrecipes

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.dialogs.IntroDialog
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.details.RecipeDetails
import zelgius.com.myrecipes.ui.details.RecipeDetailsPreview
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.home.Home
import zelgius.com.myrecipes.ui.home.HomeViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) processIntent(intent)

        setContent {
            AppTheme {
                SharedTransitionLayout {

                    val mainNavController = rememberNavController()
                    NavHost(navController = mainNavController, startDestination = "home") {
                        composable("home") {
                            Home(
                                animatedVisibilityScope = this@composable,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                onClick = {
                                    mainNavController.navigate(
                                        "details?id=${it.id}&name=${it.name}&url=${it.imageUrl}&type=${it.type.name}"
                                    ) {
                                        popUpTo(mainNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            "details?id={id}&name={name}&url={url}&type={type}",
                            arguments = listOf(
                                navArgument("id") { defaultValue = 0L },
                                navArgument("url") { defaultValue = "" },
                                navArgument("name") { defaultValue = "" },
                                navArgument("type") { defaultValue = "" },
                            )
                        ) {
                            val id = it.arguments?.getLong("id")
                            val type = it.arguments?.getString("type")?.let { t ->
                                Recipe.Type.valueOf(t)
                            }

                            if (id == null || type == null) {
                                mainNavController.popBackStack()
                                return@composable
                            }

                            RecipeDetails(
                                animatedVisibilityScope = this@composable,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                navigateBack = { mainNavController.popBackStack() },
                                viewModel = hiltViewModel(creationCallback = { factory: RecipeDetailsViewModel.Factory ->
                                    factory.create(
                                        Recipe(
                                            id,
                                            type = type,
                                            name = it.arguments?.getString("name") ?: "",
                                            imageUrl = it.arguments?.getString("url")
                                        )
                                    )
                                })
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        processIntent(intent)
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
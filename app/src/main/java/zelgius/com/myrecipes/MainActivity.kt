package zelgius.com.myrecipes

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.details.RecipeDetails
import zelgius.com.myrecipes.ui.edit.viewModel.EditRecipeViewModel
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.edit.EditRecipe
import zelgius.com.myrecipes.ui.home.Home
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                SharedTransitionLayout {
                    var selectedType by remember {
                        mutableStateOf(Recipe.Type.Meal)
                    }
                    val mainNavController = rememberNavController()

                    NavHost(navController = mainNavController, startDestination = "home") {
                        composable(
                            "home",
                            enterTransition = { fadeIn() },
                            popEnterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                            popExitTransition = { fadeOut() },
                        ) {
                            Home(
                                animatedVisibilityScope = this@composable,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                onTypeChanged = {
                                    selectedType = it
                                },
                                onClick = {
                                    if (it == null)
                                        mainNavController.navigate("edit?type=${selectedType.name}") {
                                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                        }
                                    else
                                        mainNavController.navigate(
                                            "details?id=${it.id}" +
                                                    "&name=${
                                                        URLEncoder.encode(
                                                            it.name,
                                                            Charsets.UTF_8.name()
                                                        )
                                                    }" +
                                                    (if (it.imageUrl?.takeIf { s -> s != "null" }
                                                            .isNullOrBlank()) "" else "&url=${it.imageUrl}") +
                                                    "&type=${it.type.name}"
                                        ) {
                                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                        }
                                }
                            )
                        }

                        recipeComposable(
                            enterTransition = { fadeIn() },
                            popEnterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                            popExitTransition = { fadeOut() },
                            route = "details",
                        ) {
                            RecipeDetails(
                                animatedVisibilityScope = this,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                navigateBack = { mainNavController.popBackStack() },
                                viewModel = hiltViewModel(creationCallback = { factory: RecipeDetailsViewModel.Factory ->
                                    factory.create(it)
                                }),
                                onEdit = {
                                    mainNavController.navigate(
                                        "edit?id=${it.id}&name=${it.name}&url=${it.imageUrl}&type=${it.type.name}"
                                    ) {
                                        popUpTo("details") {
                                            saveState = true
                                        }
                                    }
                                }
                            )
                        }
                        recipeComposable("edit",
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                )
                            }
                        ) {
                            EditRecipe(
                                navigateBack = { mainNavController.popBackStack() },
                                viewModel = hiltViewModel(creationCallback = { factory: EditRecipeViewModel.Factory ->
                                    factory.create(it)
                                }),
                            )
                        }

                    }
                }
            }
        }
    }

    private fun NavGraphBuilder.recipeComposable(
        route: String,
        enterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
        exitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
        popEnterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
            enterTransition,
        popExitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
            exitTransition,
        content: @Composable AnimatedContentScope.(Recipe) -> Unit
    ) {
        composable(
            "$route${if (route.contains("?")) "" else "?"}id={id}&name={name}&url={url}&type={type}",
            arguments = listOf(
                navArgument("id") { defaultValue = -1L },
                navArgument("url") { defaultValue = "" },
                navArgument("name") { defaultValue = "" },
                navArgument("type") { defaultValue = "" },
            ),
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) {
            val id = it.arguments?.getLong("id")
            val type = it.arguments?.getString("type")?.let { t ->
                Recipe.Type.valueOf(t)
            }

            if (id == null || type == null) return@composable

            content(
                Recipe(
                    if (id == -1L) null else id,
                    type = type,
                    name = it.arguments?.getString("name")
                        ?.let { s -> URLDecoder.decode(s, Charsets.UTF_8.name()) } ?: "",
                    imageUrl = it.arguments?.getString("url")
                )
            )
        }
    }
}
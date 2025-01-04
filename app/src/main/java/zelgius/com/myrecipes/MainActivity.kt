@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zelgius.billing.repository.BillingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import zelgius.com.myrecipes.MainActivity.Navigation.RecognitionSetUp
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.addFromWeb.AddFromWeb
import zelgius.com.myrecipes.ui.details.RecipeDetails
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.edit.EditRecipe
import zelgius.com.myrecipes.ui.edit.viewModel.EditRecipeViewModel
import zelgius.com.myrecipes.ui.gestureSetUp.GestureSetUpScreen
import zelgius.com.myrecipes.ui.home.Home
import zelgius.com.myrecipes.ui.home.HomeNavigation
import zelgius.com.myrecipes.ui.play.PlayRecipeActivity
import zelgius.com.myrecipes.ui.settings.Settings
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var billingRepository: BillingRepository

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            billingRepository.checkPurchase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {

        }

        setContent {
            var selectedItem: HomeNavigation by rememberSaveable {
                mutableStateOf(HomeNavigation.Recipe(Recipe.Type.Meal))
            }

            LaunchedEffect(null){
                dataStoreRepository.selectedTab.collect {
                    selectedItem = HomeNavigation.Recipe(it)
                }
            }
            AppTheme {
                SharedTransitionLayout {
                    val navigator = rememberListDetailPaneScaffoldNavigator<Navigation>()

                    BackHandler(navigator.canNavigateBack()) {
                        lifecycleScope.launch {
                            navigator.navigateBack()
                        }
                    }

                    ListDetailPaneScaffold(
                        directive = navigator.scaffoldDirective.copy(defaultPanePreferredWidth = 500.dp),
                        value = navigator.scaffoldValue,
                        listPane = {
                            AnimatedPane {
                                Home(
                                    animatedVisibilityScope = this,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    selectedItem = selectedItem,
                                    onNavigate = {
                                        if(it is HomeNavigation.Recipe) {
                                            selectedItem = it
                                            lifecycleScope.launch {
                                                dataStoreRepository.setSelectedTab(it.type)
                                            }
                                        } else selectedItem = it
                                    },
                                    onSettingsClicked = {
                                        navigator.navigateToDetails(Navigation.Settings)
                                    },
                                    onClick = {
                                        if (it != null) navigator.navigateToDetails(
                                            Navigation.Details(
                                                it
                                            )
                                        )
                                        else (selectedItem as? HomeNavigation.Recipe)?.let {
                                            navigator.navigateToDetails(Navigation.Add(it.type))
                                        }
                                    })
                            }
                        },
                        detailPane = {
                            AnimatedPane {
                                DetailsPane(
                                    navigator,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = if (navigator.scaffoldValue.secondary != PaneAdaptedValue.Hidden) null else this
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    fun ThreePaneScaffoldNavigator<Navigation>.navigateToDetails(destination: Navigation) {
        lifecycleScope.launch {
            navigateTo(
                ListDetailPaneScaffoldRole.Detail,
                destination
            )
        }
    }

    @Composable
    private fun DetailsPane(
        navigator: ThreePaneScaffoldNavigator<Navigation>,
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope?,
    ) {

        val destination = navigator.currentDestination?.contentKey
        when (destination) {
            null, Navigation.NoSelection -> NoSelection()
            is Navigation.Details -> Details(
                navigator,
                sharedTransitionScope,
                animatedVisibilityScope,
                destination.recipe
            )

            Navigation.Settings -> Settings(
                onBack = {
                    lifecycleScope.launch {
                        navigator.navigateBack()
                    }
                },
            )

            is Navigation.Add -> Add(navigator = navigator, type = destination.type)
            is Navigation.AddFromWeb -> AddFromWeb(navigator = navigator, type = destination.type)
            is Navigation.Edit -> Edit(navigator = navigator, recipe = destination.recipe)
            is RecognitionSetUp -> GestureSetUpScreen(
                onBack = {
                    lifecycleScope.launch {
                        navigator.navigateBack()
                    }
                },
            )
        }
    }

    @Composable
    private fun NoSelection() {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    "Nothing to display",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun Details(
        navigator: ThreePaneScaffoldNavigator<Navigation>,
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope?,
        recipe: Recipe
    ) {
        val mainNavController = rememberNavController()

        NavHost(
            navController = mainNavController,
            startDestination = "details?${recipe.urlParams}",
        ) {
            recipeComposable(
                route = "details",
            ) {
                RecipeDetails(
                    animatedVisibilityScope = animatedVisibilityScope,
                    isTwoPanes = navigator.scaffoldValue.secondary != PaneAdaptedValue.Hidden,
                    sharedTransitionScope = sharedTransitionScope,
                    navigateBack = {
                        lifecycleScope.launch {
                            navigator.navigateBack()
                        }
                    },
                    viewModel = hiltViewModel(creationCallback = { factory: RecipeDetailsViewModel.Factory ->
                        factory.create(it)
                    }),
                    onEdit = {
                        mainNavController.navigate("edit?${it.urlParams}")
                    },
                    playRecipe = {
                        PlayRecipeActivity.start(this@MainActivity, it.id ?: -1)
                    })
            }

            recipeComposable("edit", enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                )
            }) {
                EditRecipe(
                    navigateBack = {
                        mainNavController.popBackStack()
                    },
                    viewModel = hiltViewModel<EditRecipeViewModel>().apply {
                        init(it)
                    },
                )
            }
        }
    }

    @Composable
    fun Add(
        navigator: ThreePaneScaffoldNavigator<Navigation>,
        type: Recipe.Type
    ) {
        EditRecipe(
            navigateBack = {
                if (navigator.canNavigateBack()) lifecycleScope.launch { navigator.navigateBack() }
                else navigator.navigateToDetails(Navigation.NoSelection)
            },
            displayBack = navigator.canNavigateBack(),
            viewModel = hiltViewModel<EditRecipeViewModel>().apply {
                init(Recipe.Empty.copy(type = type))
            },
            addFromWeb = {
                navigator.navigateToDetails(Navigation.AddFromWeb(type))
            }
        )
    }

    @Composable
    fun Edit(
        navigator: ThreePaneScaffoldNavigator<Navigation>,
        recipe: Recipe
    ) {
        EditRecipe(
            navigateBack = {
                if (navigator.canNavigateBack()) lifecycleScope.launch { navigator.navigateBack() }
                else navigator.navigateToDetails(Navigation.NoSelection)
            },
            displayBack = navigator.canNavigateBack(),
            viewModel = hiltViewModel<EditRecipeViewModel>().apply {
                load(recipe)
            },
        )
    }

    private fun NavGraphBuilder.recipeComposable(
        route: String,
        enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
        exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
        popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
        popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
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
                    imageUrl = it.arguments?.getString("url")))
        }
    }

    @Parcelize
    sealed interface Navigation : Parcelable {
        object NoSelection : Navigation
        class Details(val recipe: Recipe) : Navigation

        data class Add(val type: Recipe.Type) : Navigation
        data class Edit(val recipe: Recipe) : Navigation

        object Settings : Navigation
        object RecognitionSetUp : Navigation
        data class AddFromWeb(val type: Recipe.Type) : Navigation
    }

}


val Recipe.urlParams
    get() = "id=${id}" + "&name=${
        URLEncoder.encode(
            name, Charsets.UTF_8.name()
        )
    }" + (if (imageUrl?.takeIf { s -> s != "null" }
            .isNullOrBlank()) "" else "&url=${imageUrl}") + "&type=${type.name}"

package zelgius.com.myrecipes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import zelgius.com.myrecipes.ui.details.viewModel.RecipeDetailsViewModel
import zelgius.com.myrecipes.ui.edit.EditRecipe
import zelgius.com.myrecipes.ui.edit.viewModel.EditRecipeViewModel
import zelgius.com.myrecipes.ui.home.Home
import zelgius.com.myrecipes.ui.play.PlayRecipeActivity
import zelgius.com.myrecipes.ui.settings.Settings
import zelgius.com.myrecipes.utils.isTwoPanes
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.collections.listOf

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                SharedTransitionLayout {

                    var selectedType by remember {
                        mutableStateOf(Recipe.Type.Meal)
                    }
                    val mainNavController = rememberNavController()
                    val twoPanes = isTwoPanes()

                    LaunchedEffect(twoPanes) {
                        if (!twoPanes) {
                            mainNavController.navigate("home"){
                                popUpTo(0)
                            }
                        } else {
                            mainNavController.navigate("no_selection") {
                                popUpTo(0)
                            }
                        }
                    }

                    @Composable
                    fun AnimatedVisibilityScope.home(
                        animatedVisibilityScope: AnimatedVisibilityScope? = null,
                        sharedTransitionScope: SharedTransitionScope? = null
                    ) {
                        Home(animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            onTypeChanged = {
                                selectedType = it
                            },
                            onSettingsClicked = {
                                mainNavController.navigate("settings")
                            },
                            onClick = {
                                if (it == null) mainNavController.navigate("edit?type=${selectedType.name}") {
                                    popUpTo(mainNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                                else mainNavController.navigate("details?${it.urlParams}") {
                                    popUpTo(mainNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            })
                    }

                    val startDestination = if (!twoPanes) "home" else "no_selection"

                    Row {
                        val navHostModifier = if (twoPanes) Modifier.weight(2f) else Modifier
                        if (twoPanes) AnimatedVisibility(true, modifier = Modifier.weight(1f)) {
                            home()
                        }

                        NavHost(
                            navController = mainNavController,
                            startDestination = startDestination,
                            modifier = navHostModifier
                        ) {
                            composable(
                                "no_selection",
                            ) {
                                Scaffold(topBar = {
                                    TopAppBar(
                                        title = {},
                                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                                        actions = {
                                            IconButton(onClick = {
                                                mainNavController.navigate("settings")
                                            }) {
                                                Icon(
                                                    Icons.TwoTone.Settings,
                                                    modifier = Modifier.padding(8.dp),
                                                    contentDescription = stringResource(
                                                        id = R.string.scan_recipe,
                                                    )
                                                )
                                            }
                                        })
                                }) { padding ->

                                    Box(
                                        modifier = Modifier
                                            .padding(padding)
                                            .fillMaxSize()
                                    ) {
                                        Text(
                                            "Nothing to display",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                            composable(
                                "home",
                                enterTransition = { fadeIn() },
                                popEnterTransition = { fadeIn() },
                                exitTransition = { fadeOut() },
                                popExitTransition = { fadeOut() },
                            ) {
                                home(
                                    animatedVisibilityScope = this@composable,
                                    sharedTransitionScope = this@SharedTransitionLayout
                                )
                            }

                            recipeComposable(
                                enterTransition = {
                                    if (!twoPanes) fadeIn() else slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Start
                                    )
                                },
                                exitTransition = {
                                    if (!twoPanes) fadeOut() else slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.End
                                    )
                                },
                                route = "details",
                            ) {
                                RecipeDetails(animatedVisibilityScope = this,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    navigateBack = { mainNavController.popBackStack() },
                                    viewModel = hiltViewModel(creationCallback = { factory: RecipeDetailsViewModel.Factory ->
                                        factory.create(it)
                                    }),
                                    onEdit = {
                                        mainNavController.navigate("edit?${it.urlParams}") {
                                            popUpTo("details") {
                                                saveState = true
                                            }
                                        }
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
                                    navigateBack = { mainNavController.popBackStack() },
                                    viewModel = hiltViewModel(creationCallback = { factory: EditRecipeViewModel.Factory ->
                                        factory.create(it)
                                    }),
                                )
                            }

                            composable("settings") {
                                Settings(onBack = { mainNavController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }

    val Recipe.urlParams
        get() = "id=${id}" + "&name=${
            URLEncoder.encode(
                name, Charsets.UTF_8.name()
            )
        }" + (if (imageUrl?.takeIf { s -> s != "null" }
                .isNullOrBlank()) "" else "&url=${imageUrl}") + "&type=${type.name}"

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

            content(Recipe(if (id == -1L) null else id,
                type = type,
                name = it.arguments?.getString("name")
                    ?.let { s -> URLDecoder.decode(s, Charsets.UTF_8.name()) } ?: "",
                imageUrl = it.arguments?.getString("url")))
        }
    }


}

/*
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    fun startWorkerIfNeeded() {
        val worker = OneTimeWorkRequestBuilder<ImageGenerationWorker>()
            .setInputData(
                Data.Builder().build()
            )
            .addTag(ImageGenerationWorker.TAG)
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag(ImageGenerationWorker.TAG)
            enqueue(worker)
        }
    }
}*/
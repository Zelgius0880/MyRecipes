@file:OptIn(ExperimentalSharedTransitionApi::class)

package zelgius.com.myrecipes.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.VisionBarcodeReaderActivity
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.SnackBar
import zelgius.com.myrecipes.ui.preview.SharedElementPreview
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.recipe.RecipeList

val tabs = listOf(Recipe.Type.Meal, Recipe.Type.Dessert, Recipe.Type.Other)

@Composable
fun Home(
    viewModel: HomeViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onTypeChanged: (Recipe.Type) -> Unit = {},
    onClick: (Recipe?) -> Unit = {},
) {
    val scanQrLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.getStringExtra("BASE64")?.let { s ->
                viewModel.addRecipeFromQr(s)
            }
        }
    val context = LocalContext.current

    HomeView(
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        pageMeals = viewModel.mealsPage,
        pageDesserts = viewModel.dessertPage,
        pageOther = viewModel.otherPage,
        onTypeChanged = onTypeChanged,
        onClick = onClick,
        onScanClicked = {
            scanQrLauncher.launch(Intent(context, VisionBarcodeReaderActivity::class.java))
        },
        onRemove = {
            viewModel.removeRecipe(it)
        },
        onRestore = {
            viewModel.restoreRecipe(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeView(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Recipe?) -> Unit = {},
    onRemove: (Recipe) -> Flow<Recipe> = { emptyFlow() },
    onRestore: (Recipe) -> Unit = {},
    onScanClicked: () -> Unit = {},
    onTypeChanged: (Recipe.Type) -> Unit = {},
    pageMeals: Flow<PagingData<Recipe>>,
    pageDesserts: Flow<PagingData<Recipe>>,
    pageOther: Flow<PagingData<Recipe>>,
) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val removeRecipe: (Recipe) -> Unit = {
        scope.launch {
            val backup = onRemove(it).first()

            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.recipe_removed),
                actionLabel = context.getString(R.string.undo)
            )
            if(result == SnackbarResult.ActionPerformed)
                onRestore(backup)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.recipe_list)) },
                actions = {
                    IconButton(onClick = onScanClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_code_white),
                            modifier = Modifier.padding(8.dp),
                            contentDescription = stringResource(
                                id = R.string.search_hint,
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onClick(null)
            }) {
                Icon(Icons.TwoTone.Add, "Add")
            }
        },
        content = { padding ->
            val navController = rememberNavController()
            var selectedTabIndex by rememberSaveable {
                mutableIntStateOf(0)
            }
            var oldTabIndex by remember {
                mutableIntStateOf(0)
            }
            Column(modifier = Modifier.padding(padding)) {
                TabRow(selectedTabIndex = selectedTabIndex, indicator = {
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]))
                }) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(selected = index == selectedTabIndex, onClick = {
                            if (selectedTabIndex == index) return@Tab

                            oldTabIndex = selectedTabIndex
                            selectedTabIndex = index

                            onTypeChanged(tab)
                            navController.navigate(
                                when (tab) {
                                    Recipe.Type.Meal -> "meals"
                                    Recipe.Type.Dessert -> "desserts"
                                    Recipe.Type.Other -> "other"
                                }
                            )
                        }) {
                            Text(
                                text = tab.string(),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                NavHost(navController = navController, startDestination = "meals") {
                    fun NavGraphBuilder.tabComposable(
                        route: String,
                        content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
                    ) = composable(
                        route = route,
                        content = content,
                        enterTransition = {
                            slideIntoContainer(
                                if (oldTabIndex < selectedTabIndex) AnimatedContentTransitionScope.SlideDirection.End
                                else AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(500)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                if (oldTabIndex < selectedTabIndex) AnimatedContentTransitionScope.SlideDirection.End
                                else AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(500)
                            )
                        }
                    )

                    tabComposable("meals") {
                        RecipeList(
                            onClick = onClick,
                            onRemove = removeRecipe,
                            list = pageMeals,
                            modifier = Modifier.fillMaxSize(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    tabComposable("desserts") {
                        RecipeList(
                            onClick = onClick,
                            onRemove = removeRecipe,
                            list = pageDesserts,
                            modifier = Modifier.fillMaxSize(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    tabComposable("other") {
                        RecipeList(
                            onClick = onClick,
                            onRemove = removeRecipe,
                            list = pageOther,
                            modifier = Modifier.fillMaxSize(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }
            }
        }
    )
}

@Composable
@Preview
fun HomePreview() {
    fun createSample(index: Int) =
        flowOf(
            PagingData.from((1..6).map {
                createDummyModel(id = (6 * index + it).toLong(), suffix = " ${6 * index + it}")
            })
        )

    SharedElementPreview { animatedVisibilityScope, sharedTransitionScope ->
        HomeView(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            pageMeals = createSample(0),
            pageDesserts = createSample(1),
            pageOther = createSample(2),
        )
    }
}

@Composable
fun Recipe.Type.string() = stringResource(
    id = when (this) {
        Recipe.Type.Dessert -> R.string.dessert
        Recipe.Type.Meal -> R.string.meal
        Recipe.Type.Other -> R.string.other
    }
)
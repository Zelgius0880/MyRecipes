@file:OptIn(ExperimentalSharedTransitionApi::class)

package zelgius.com.myrecipes.ui.home

import android.content.Intent
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.BakeryDining
import androidx.compose.material.icons.twotone.Cake
import androidx.compose.material.icons.twotone.LunchDining
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.VisionBarcodeReaderActivity
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.recipe.RecipeList
import zelgius.com.myrecipes.ui.preview.SharedElementPreview
import zelgius.com.myrecipes.ui.preview.createDummyModel
import zelgius.com.myrecipes.utils.hasNavigationBar
import zelgius.com.myrecipes.utils.hasNavigationRail

val tabs = listOf(
    HomeNavigation.Recipe(Recipe.Type.Meal),
    HomeNavigation.Recipe(Recipe.Type.Dessert),
    HomeNavigation.Recipe(Recipe.Type.Other),
)

@Composable
fun Home(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
    selectedItem: HomeNavigation = tabs.first(),
    onNavigate: (HomeNavigation) -> Unit = {},
    onSettingsClicked: () -> Unit,
    onClick: (Recipe?) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scanQrLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.getStringExtra("BASE64")?.let { s ->
                coroutineScope.launch {
                    viewModel.addRecipeFromQr(s)
                    snackbarHostState.showSnackbar(context.getString(R.string.recipe_saved))
                }
            }
        }

    HomeView(
        modifier = modifier,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        snackbarHostState = snackbarHostState,
        pageMeals = viewModel.mealsPage,
        pageDesserts = viewModel.dessertPage,
        pageOther = viewModel.otherPage,
        onNavigate = onNavigate,
        selectedItem = selectedItem,
        onClick = onClick,
        onScanClicked = {
            scanQrLauncher.launch(Intent(context, VisionBarcodeReaderActivity::class.java))
        },
        onSettingsClicked = onSettingsClicked,
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
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
    selectedItem: HomeNavigation = tabs.first(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onClick: (Recipe?) -> Unit = {},
    onRemove: (Recipe) -> Flow<Recipe> = { emptyFlow() },
    onRestore: (Recipe) -> Unit = {},
    onScanClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onNavigate: (HomeNavigation) -> Unit = {},
    pageMeals: Flow<PagingData<Recipe>>,
    pageDesserts: Flow<PagingData<Recipe>>,
    pageOther: Flow<PagingData<Recipe>>,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val removeRecipe: (Recipe) -> Unit = {
        scope.launch {
            val backup = onRemove(it).first()

            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.recipe_removed),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed)
                onRestore(backup)
        }
    }

    val hasNavigationRail = hasNavigationRail()

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            if (hasNavigationRail) {
                item(
                    icon = {
                        FloatingActionButton(
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                            onClick = {
                                onClick(null)
                            }) {
                            Icon(Icons.TwoTone.Add, "Add")
                        }
                    },
                    selected = false,
                    enabled = false,
                    alwaysShowLabel = true,
                    onClick = {
                        onClick(null)
                    }
                )

                spacerItem()
            }

            tabs.forEach {
                item(
                    icon = {
                        AnimatedContent(it == selectedItem) { selected ->
                            Icon(
                                it.icon(selected),
                                contentDescription = it.string()
                            )
                        }
                    },
                    label = { Text(it.string()) },
                    selected = it == selectedItem,
                    onClick = {
                        onNavigate(it)
                    }
                )
            }

            if (hasNavigationRail) {
                spacerItem()

                item(
                    icon = {
                        Icon(
                            Icons.TwoTone.Settings,
                            modifier = Modifier.padding(8.dp),
                            contentDescription = stringResource(
                                id = R.string.settings,
                            )
                        )
                    },
                    selected = false,
                    label = { Text(stringResource(id = R.string.settings)) },
                    onClick = onSettingsClicked
                )

                item(
                    icon = {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = stringResource(R.string.scan_recipe)
                        )
                    },
                    selected = false,
                    label = { Text(stringResource(id = R.string.scan_recipe)) },
                    onClick = {
                        onScanClicked()
                    }
                )

            }
        },
    ) {

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                if (hasNavigationBar())
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.recipe_list)) },
                        actions = {
                            IconButton(onClick = onScanClicked) {
                                Icon(
                                    Icons.Filled.QrCodeScanner,
                                    modifier = Modifier.padding(8.dp),
                                    contentDescription = stringResource(
                                        id = R.string.scan_recipe,
                                    )
                                )
                            }

                            IconButton(onClick = onSettingsClicked) {
                                Icon(
                                    Icons.TwoTone.Settings,
                                    modifier = Modifier.padding(8.dp),
                                    contentDescription = stringResource(
                                        id = R.string.settings,
                                    )
                                )
                            }
                        }
                    )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    if (hasNavigationBar())
                        FloatingActionButton(onClick = {
                            onClick(null)
                        }) {
                            Icon(Icons.TwoTone.Add, "Add")
                        }
                }

            },
            content = { padding ->

                AnimatedContent(targetState = selectedItem,
                    transitionSpec = {
                        val oldIndex = initialState.let {
                            tabs.indexOf(it)
                        }

                        val newIndex = targetState.let {
                            tabs.indexOf(it)
                        }

                        when {
                            oldIndex < newIndex -> {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                                ).togetherWith(
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.End,
                                        animationSpec = tween(300)
                                    )
                                )
                            }

                            oldIndex > newIndex -> {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                                ).togetherWith(
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Start,
                                        animationSpec = tween(300)
                                    )
                                )
                            }

                            else -> (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(220, delayMillis = 90)
                                    ))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        }
                    }
                ) {
                    RecipeList(
                        onClick = onClick,
                        onRemove = removeRecipe,
                        list = when ((it as? HomeNavigation.Recipe)?.type) {
                            Recipe.Type.Meal -> pageMeals
                            Recipe.Type.Dessert -> pageDesserts
                            Recipe.Type.Other -> pageOther
                            else -> emptyFlow()
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }
            }
        )
    }
}

private fun NavigationSuiteScope.spacerItem() {
    item(
        icon = {},
        selected = false,
        alwaysShowLabel = true,
        enabled = false,
        label = { Spacer(modifier = Modifier.height(32.dp)) },
        onClick = {}
    )
}


@Composable
fun Recipe.Type.string() = stringResource(
    id = when (this) {
        Recipe.Type.Dessert -> R.string.dessert
        Recipe.Type.Meal -> R.string.meal
        Recipe.Type.Other -> R.string.other
    }
)


sealed interface HomeNavigation : Parcelable {

    @Parcelize
    object Settings : HomeNavigation

    @Parcelize
    data class Recipe(val type: Recipe.Type) : HomeNavigation
}

@Composable
fun HomeNavigation.string() = when (this) {
    HomeNavigation.Settings -> stringResource(id = R.string.settings)
    is HomeNavigation.Recipe -> type.string()
}

fun HomeNavigation.Recipe.icon(selected: Boolean) = if (selected)
    when (type) {
        Recipe.Type.Dessert -> Icons.Filled.Cake
        Recipe.Type.Meal -> Icons.Filled.LunchDining
        Recipe.Type.Other -> Icons.Filled.BakeryDining
    }
else
    when (type) {
        Recipe.Type.Dessert -> Icons.TwoTone.Cake
        Recipe.Type.Meal -> Icons.TwoTone.LunchDining
        Recipe.Type.Other -> Icons.TwoTone.BakeryDining
    }


@Composable
@PreviewScreenSizes
fun HomePreview() {
    fun createSample(index: Int) =
        flowOf(
            PagingData.from((1..6).map {
                createDummyModel(id = (6 * index + it).toLong(), suffix = " ${6 * index + it}")
            })
        )

    AppTheme {
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
}

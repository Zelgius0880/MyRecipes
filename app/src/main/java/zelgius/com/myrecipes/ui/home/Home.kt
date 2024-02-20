package zelgius.com.myrecipes.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.recipe.RecipeList

val tabs = listOf(Recipe.Type.Meal, Recipe.Type.Dessert, Recipe.Type.Other)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Home(viewModel: HomeViewModel = hiltViewModel()) {

    val pageState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.recipe_list)) },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search_24dp),
                                contentDescription = stringResource(
                                    id = R.string.search_hint,
                                )
                            )
                        }

                        IconButton(onClick = { /*TODO*/ }) {
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
                TabRow(selectedTabIndex = pageState.currentPage, indicator = {
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[pageState.currentPage]))
                }) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(selected = index == pageState.currentPage, onClick = {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(index)
                            }
                        }) {
                            Text(text = tab.string(), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        content = { padding ->
            HorizontalPager(state = pageState, contentPadding = padding) { page ->

                when (page) {
                    0 -> RecipeList(list = viewModel.mealsPage)

                    1 -> RecipeList(list = viewModel.dessertPage)

                    else -> RecipeList(list = viewModel.otherPage)
                }
            }
        }
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
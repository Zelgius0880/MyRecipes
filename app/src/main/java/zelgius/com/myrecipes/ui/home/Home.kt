package zelgius.com.myrecipes.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.ui.AppTheme

val tabs = listOf(Recipe.Type.Meal, Recipe.Type.Dessert, Recipe.Type.Other)

@Composable
fun Home() {
    var selectedTab by remember {
        mutableIntStateOf(0)
    }
    var oldTab by remember {
        mutableIntStateOf(0)
    }

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
                TabRow(selectedTabIndex = selectedTab, indicator = {
                    TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(it[selectedTab]))
                }) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(selected = index == selectedTab, onClick = {
                            selectedTab = index
                        }) {
                            Text(text = tab.string(), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        content = {
            Text("Hello", modifier = Modifier.padding(it))
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

@Preview
@Composable
fun HomePreview() {
    AppTheme {
        Home()
    }
}
package zelgius.com.myrecipes.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.model.asIngredient
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.LinkableText
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.ingredients.UpdateIngredient
import zelgius.com.myrecipes.utils.hasNavigationRail

@Composable
fun Settings(viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit) {
    val isIAGenerationChecked by viewModel.isIAGenerationChecked.collectAsStateWithLifecycle(false)
    val isIAGenerationEnabled by viewModel.isIAGenerationEnabled.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle(emptyList())
    var selectedIngredient by remember {
        mutableStateOf<SimpleIngredient?>(null)
    }

    Settings(
        isIAGenerationChecked = isIAGenerationChecked,
        isIAGenerationEnabled = isIAGenerationEnabled,
        onIAGenerationChanged = viewModel::setIsIAGenerationChecked,
        ingredients = ingredients,
        onDeleteIngredient = viewModel::deleteIngredient,
        onUpdateIngredient = {
            selectedIngredient = it
        },
        onBack = onBack
    )

    AnimatedVisibility(selectedIngredient != null) {
        selectedIngredient?.let {
            UpdateIngredient(
                ingredient = it,
                onDismiss = {
                    selectedIngredient = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(
    isIAGenerationChecked: Boolean = false,
    isIAGenerationEnabled: Boolean = false,
    onIAGenerationChanged: (Boolean) -> Unit = {},
    ingredients: List<SimpleIngredient> = emptyList(),
    onDeleteIngredient: (SimpleIngredient) -> Unit = {},
    onUpdateIngredient: (SimpleIngredient) -> Unit = {},
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {

                    if (!hasNavigationRail())
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                                contentDescription = ""
                            )
                        }
                }
            )
        },

        ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(vertical = 8.dp),
        ) {
            item {
                IAGenerationSwitch(
                    isIAGenerationChecked,
                    isIAGenerationEnabled,
                    onIAGenerationChanged
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.ingredients),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                )
            }

            itemsIndexed(ingredients, key = { _, item -> item.id }) { index, item ->
                Column(
                    modifier = Modifier
                        .animateItem()
                        .clickable { onUpdateIngredient(item) }
                        .padding(top = 8.dp)
                    ,
                ) {
                    SettingsIngredient(ingredient = item, onDeleteIngredient = onDeleteIngredient)

                    if (index < ingredients.lastIndex)
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp)
                        )
                }
            }
        }
    }

}

@Composable
private fun IAGenerationSwitch(
    isIAGenerationChecked: Boolean,
    isIAGenerationEnabled: Boolean,
    onIAGenerationChanged: (Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.ia_generation),
                modifier = Modifier
                    .weight(1f)
                    .align(
                        Alignment.CenterVertically
                    )
            )
            Switch(isIAGenerationChecked, onIAGenerationChanged, enabled = isIAGenerationEnabled)
        }
        if (!isIAGenerationEnabled)
            LinkableText(
                modifier = Modifier.fillMaxWidth(),
                id = R.string.ia_generation_disabled,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error)
            ) {
                uriHandler.openUri(it)
            }
    }
}

@Composable
private fun SettingsIngredient(
    ingredient: SimpleIngredient,
    modifier: Modifier = Modifier,
    onDeleteIngredient: (SimpleIngredient) -> Unit = {},
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Ingredient(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .heightIn(min = 48.dp),
            ingredient = ingredient.asIngredient(),
            text = ingredient.name
        )

        if (ingredient.removable) {
            IconButton(onClick = { onDeleteIngredient(ingredient) }) {
                Icon(
                    imageVector = Icons.TwoTone.Delete,
                    contentDescription = ""
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    AppTheme {
        Settings(
            isIAGenerationEnabled = false,
            ingredients = List(5) {
                SimpleIngredient(
                    it.toLong(),
                    "Ingredient $it",
                    null,
                    it % 2 == 0
                )
            }
        )
    }
}


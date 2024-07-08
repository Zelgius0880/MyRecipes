@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.net.Uri
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.chip.Chip
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.Step
import zelgius.com.myrecipes.ui.edit.viewModel.AddIngredient
import zelgius.com.myrecipes.ui.edit.viewModel.AddStep
import zelgius.com.myrecipes.ui.edit.viewModel.EditRecipeViewModel
import zelgius.com.myrecipes.ui.edit.viewModel.IngredientItem
import zelgius.com.myrecipes.ui.edit.viewModel.ListItem
import zelgius.com.myrecipes.ui.edit.viewModel.StepItem


@Composable
fun EditRecipe(viewModel: EditRecipeViewModel, navigateBack: () -> Unit = {}) {

    val recipe by viewModel.recipeFlow.collectAsState()
    val items by viewModel.itemsFlow.collectAsState(emptyList())
    EditRecipeView(recipe,
        items,
        navigateBack,
        onNameChanged = viewModel::changeName,
        onImageUrlChanged = {
            viewModel.changeImageUrl(it.toString())
        })
}

private enum class Action { Update, Add, Delete }

@Composable
private fun EditRecipeView(
    recipe: Recipe,
    items: List<ListItem>,
    navigateBack: () -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onImageUrlChanged: (Uri) -> Unit = {},
    onActionOnStep: (Action, step: Step?) -> Unit = { _, _ -> },
    onActionOnIngredient: (Action, ingredient: Ingredient?) -> Unit = { _, _ -> }
) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                if (recipe.id == null) stringResource(R.string.new_recipe)
                else stringResource(R.string.edit_recipe)
            )
        }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = ""
                )
            }
        }, actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.TwoTone.Check, contentDescription = ""
                )
            }
        })
    }, content = { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                EditRecipeHeader(
                    name = recipe.name,
                    imageUrl = recipe.imageUrl,
                    onNameChanged = onNameChanged,
                    onImageUrlChanged = onImageUrlChanged
                )
            }

            items(items) { item ->
                when (item) {
                    is IngredientItem -> IngredientItem(item)
                    is StepItem -> StepItem(item)
                    is AddIngredient -> AddIngredient {
                        onActionOnIngredient(Action.Add, null)
                    }

                    is AddStep -> AddStep {
                        onActionOnStep(Action.Add, null)
                    }
                }
            }
        }
    })
}


@Composable
private fun EditRecipeHeader(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onNameChanged: (String) -> Unit = {},
    onImageUrlChanged: (Uri) -> Unit = {},
) {
    var imageUrlState by remember {
        mutableStateOf(
            TextFieldValue(
                if (imageUrl?.startsWith("content://") == true) "" else imageUrl ?: ""
            )
        )
    }

    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                uri?.let { onImageUrlChanged(it) }
                imageUrlState = imageUrlState.copy(text = "")
            })


    Card(modifier.padding(8.dp), shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = CenterVertically
        ) {

            Image(
                painter = rememberAsyncImagePainter(
                    imageUrl, error = painterResource(R.drawable.ic_dish)
                ), contentDescription = null, modifier = Modifier
                    .size(128.dp)
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge
                    ), contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(8.dp)) {
                AppTextField(value = name,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.name)) })

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = CenterVertically) {
                    FilledIconButton(onClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_insert_photo_24),
                            contentDescription = ""
                        )
                    }

                    AppTextField(
                        value = imageUrlState, singleLine = true,
                        onValueChange = {
                            imageUrlState = imageUrlState.copy(text = it.text)
                            if (URLUtil.isValidUrl(it.text)) {
                                onImageUrlChanged(it.text.toUri())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    imageUrlState = imageUrlState.copy(
                                        selection = TextRange(
                                            0, imageUrlState.text.length
                                        )
                                    )
                                }
                            },
                        label = { Text(stringResource(R.string.image_url)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    item: StepItem,
    onEdit: (Step) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp), shape = MaterialTheme.shapes.extraLarge
    ) {
        Row {
            Step(
                item.step, modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            val count = item.step.ingredients.size
            AnimatedContent(count, label = "ingredient_chip") {
                if (it > 0)
                    Badge(
                        modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    .size(16.dp),
                                painter = painterResource(R.drawable.ic_carrot_solid),
                                contentDescription = null
                            )
                            Text(
                                text = "$it",
                            )
                        }
                        /*Text(
                            text = "$it",
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
                        )
                        Icon(
                            modifier = Modifier.padding(end = 4.dp, top = 4.dp, bottom = 4.dp).size(16.dp),
                            painter = painterResource(R.drawable.ic_carrot_solid),
                            contentDescription = null
                        )*/
                    }

            }
        }
    }
}

@Composable
private fun IngredientItem(
    item: IngredientItem,
    onEdit: (Step) -> Unit = {}
) {

    val shape = when {
        item.isFirst -> MaterialTheme.shapes.extraLarge.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize
        )

        else -> RoundedCornerShape(0.dp)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), shape = shape
    ) {
        Ingredient(item.ingredient, modifier = Modifier.padding(8.dp))
    }
}


@Composable
private fun AddIngredient(
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 8.dp, start = 8.dp),
        shape = MaterialTheme.shapes.extraLarge.copy(
            topStart = ZeroCornerSize,
            topEnd = ZeroCornerSize
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp, bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = CenterVertically) {
                Icon(imageVector = Icons.TwoTone.Add, contentDescription = "")
                Text(stringResource(R.string.add_ingredient))
            }
        }
    }
}


@Composable
private fun AddStep(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.align(Alignment.CenterEnd),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = CenterVertically) {
                Icon(imageVector = Icons.TwoTone.Add, contentDescription = "")
                Text(stringResource(R.string.add_step))
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun EditRecipePreview() {
    val recipe = createDummyModel(suffix = "")
    MaterialTheme {
        Surface {
            EditRecipeView(recipe = recipe, items = recipe.ingredients.mapIndexed { index, item ->
                IngredientItem(
                    item, isFirst = index == 0, isLast = index == recipe.ingredients.lastIndex
                )
            } + AddIngredient + recipe.steps.map { s -> StepItem(s) } + AddStep)
        }
    }
}


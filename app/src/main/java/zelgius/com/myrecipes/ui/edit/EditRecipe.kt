@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package zelgius.com.myrecipes.ui.edit

import android.content.res.Configuration
import android.net.Uri
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.RemovableItem
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.common.recipe.IngredientChip
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
        },
        onActionOnStep = { action ->
            when (action) {
                is Action.Add -> viewModel.addStep(action.item.step)
                is Action.Update -> viewModel.updateStep(action.oldItem, action.newItem)
                is Action.Delete -> viewModel.deleteStep(action.item.step)
            }
        },
        onActionOnIngredient = { action ->
            when (action) {
                is Action.Add -> viewModel.addIngredient(action.item)
                is Action.Update -> viewModel.updateIngredient(action.oldItem, action.newItem)
                is Action.Delete -> viewModel.deleteIngredient(action.item)
            }
        })
}

private sealed interface Action<T> {
    data class Add<T>(val item: T) : Action<T>
    data class Delete<T>(val item: T) : Action<T>
    data class Update<T>(val oldItem: T, val newItem: T) : Action<T>
}

@Composable
private fun EditRecipeView(
    recipe: Recipe,
    items: List<ListItem>,
    navigateBack: () -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onImageUrlChanged: (Uri) -> Unit = {},
    onActionOnStep: (Action<StepItem>) -> Unit = { },
    onActionOnIngredient: (Action<Ingredient>) -> Unit = { }
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

        var showStepBottomSheet by remember {
            mutableStateOf(false)
        }

        var selectedStep: StepItem? by remember {
            mutableStateOf(null)
        }

        var showIngredientBottomSheet by remember {
            mutableStateOf(false)
        }

        var selectedIngredient: Ingredient? by remember {
            mutableStateOf(null)
        }

        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                EditRecipeHeader(
                    name = recipe.name,
                    imageUrl = recipe.imageUrl,
                    onNameChanged = onNameChanged,
                    onImageUrlChanged = onImageUrlChanged
                )
            }

            items(items, key = { it.hashCode() }) { item ->
                when (item) {
                    is IngredientItem -> IngredientItem(item, onRemove = {
                        onActionOnIngredient(Action.Delete(it))
                    }, onEdit = {
                        selectedIngredient = it
                        showIngredientBottomSheet = true
                    })

                    is StepItem -> StepItem(item, onEdit = {
                        selectedStep = it
                        showStepBottomSheet = true
                    }, onRemove = {
                        onActionOnStep(Action.Delete(it))
                    })

                    is AddIngredient -> AddIngredientButton {
                        selectedIngredient = null
                        showIngredientBottomSheet = true
                    }

                    is AddStep -> AddStep {
                        selectedStep = null
                        showStepBottomSheet = true
                    }
                }
            }
        }

        val stepSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        if (showStepBottomSheet) StepBottomSheet(initialStep = selectedStep,
            modalBottomSheetState = stepSheetState,
            recipe = recipe,
            onSaved = {
                val selected = selectedStep
                onActionOnStep(
                    if (selected == null) Action.Add(it) else Action.Update(
                        selected, it
                    )
                )
                showStepBottomSheet = false
            },
            onDismiss = {
                showStepBottomSheet = false
            })

        val ingredientSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        if (showIngredientBottomSheet) IngredientBottomSheet(initialIngredient = selectedIngredient,
            modalBottomSheetState = ingredientSheetState,
            onSaved = {
                val selected = selectedIngredient
                onActionOnIngredient(
                    if (selected == null) Action.Add(it) else Action.Update(
                        selected, it
                    )
                )
                showIngredientBottomSheet = false
            },
            onDismiss = {
                showIngredientBottomSheet = false
            })
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

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { onImageUrlChanged(it) }
            imageUrlState = imageUrlState.copy(text = "")
        })


    Card(modifier.padding(8.dp), shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = CenterVertically
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
                AppTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.name)) },
                )

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
                            imageUrlState = imageUrlState.copy(
                                text = it.text, selection = TextRange(
                                    it.text.length, it.text.length
                                )
                            )

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
                        label = {
                            Text(
                                stringResource(R.string.image_url),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.StepItem(
    item: StepItem, onEdit: (StepItem) -> Unit = {}, onRemove: (step: StepItem) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp), shape = MaterialTheme.shapes.extraLarge
    ) {
        RemovableItem(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable { onEdit(item) }, onRemove = { onRemove(item) }) {
            Row {
                Step(
                    item.step, modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                )
                val count = item.ingredients.size
                AnimatedContent(count, label = "ingredient_chip") {
                    if (it > 0) IngredientChip(it)
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.IngredientItem(
    item: IngredientItem,
    onEdit: (ingredient: Ingredient) -> Unit = {},
    onRemove: (ingredient: Ingredient) -> Unit = {}
) {

    val shape = when {
        item.isFirst -> MaterialTheme.shapes.extraLarge.copy(
            bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize
        )

        else -> RoundedCornerShape(0.dp)
    }

    var isEdit by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateItem()
            .height(IntrinsicSize.Max)
            .padding(horizontal = 8.dp), shape = shape
    ) {
        RemovableItem(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEdit(item.ingredient)
            },
            onRemove = { onRemove(item.ingredient) }) {
            Ingredient(
                item.ingredient, modifier = Modifier.padding(8.dp)
            )

        }
    }
}


@Composable
private fun LazyItemScope.AddIngredientButton(
    onAddIngredient: () -> Unit = {}
) {
    var showAddIngredient by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateItem()
            .padding(bottom = 4.dp, end = 8.dp, start = 8.dp),
        shape = MaterialTheme.shapes.extraLarge.copy(
            topStart = ZeroCornerSize, topEnd = ZeroCornerSize
        )
    ) {
        AnimatedContent(
            showAddIngredient, label = "add_ingredient", modifier = Modifier.align(Alignment.End)
        ) {
            if (!it) {
                Button(
                    onClick = { showAddIngredient = true },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Row(verticalAlignment = CenterVertically) {
                        Icon(imageVector = Icons.TwoTone.Add, contentDescription = "")
                        Text(stringResource(R.string.add_ingredient))
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    onAddIngredient()
                }
            }
        }
    }
}


@Composable
private fun LazyItemScope.AddStep(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateItem()
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
            } + AddIngredient + recipe.steps.map { s -> StepItem(s, emptyList()) } + AddStep)
        }
    }
}


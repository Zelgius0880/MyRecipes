package zelgius.com.myrecipes.ui.ingredients

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.drawable
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.AppLabeledSwitch
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.Avatar
import zelgius.com.myrecipes.ui.common.LocalStepCardValues
import zelgius.com.myrecipes.utils.conditional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateIngredient(
    viewModel: UpdateIngredientViewModel = hiltViewModel(),
    ingredient: SimpleIngredient,
    onDismiss: () -> Unit
) {
    LaunchedEffect(null) {
        viewModel.init(ingredient)
    }

    val ingredientFlow by viewModel.ingredientFlow.collectAsStateWithLifecycle(ingredient)
    val isIaGenerationEnabled by viewModel.isIaGenerationEnabled.collectAsState(false)


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Card(modifier = Modifier.width(300.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(stringResource(R.string.update_ingredient))
                UpdateIngredient(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    ingredient = ingredientFlow,
                    isIaGenerationEnabled = isIaGenerationEnabled,
                    onIaGenerationChanged = { viewModel.onGenerationEnabledChanged(it) },
                    onNameChanged = { viewModel.onNameChanged(it) },
                    onPromptChanged = { viewModel.onPromptChanged(it) },
                    onImageChanged = { viewModel.onImageChanged(it) },
                    onGeneratedImageReset = { viewModel.onGeneratedImageReset() }
                )

                TextButton(modifier = Modifier.align(Alignment.End), onClick = {
                    viewModel.save()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.save))
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateIngredient(
    ingredient: SimpleIngredient,
    modifier: Modifier = Modifier,
    isIaGenerationEnabled: Boolean = false,
    onIaGenerationChanged: (Boolean) -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onPromptChanged: (String) -> Unit = {},
    onImageChanged: (DefaultIngredients?) -> Unit = {},
    onGeneratedImageReset: () -> Unit = {},
) {
    var showPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
    ) {
        Row {
            Box {
                val avatar = ingredient.drawable?.let {
                    Avatar.StaticImage(it)
                } ?: Avatar.Image(
                    letter = (ingredient.name.firstOrNull() ?: ' ').uppercase(),
                    ingredient.imageUrl
                )
                Avatar(
                    avatar,
                    Modifier
                        .clip(CircleShape)
                        .conditional(avatar is Avatar.Image && avatar.imageUrl != null){
                            padding(4.dp)
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        ) {
                            showPopup = true
                        }
                )


                DropdownMenu(
                    expanded = showPopup,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    onDismissRequest = { showPopup = false }) {
                    IngredientImageGrid(
                        text = ingredient.name.first().uppercase(),
                        modifier = Modifier.size(172.dp)
                    ) {
                        showPopup = false
                        onImageChanged(it)
                    }
                }
            }

            AppTextField(
                value = ingredient.name,
                label = { Text(stringResource(R.string.name)) },
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
            )
        }

        if (isIaGenerationEnabled && ingredient.drawable == null) {
            AppLabeledSwitch(
                label = stringResource(R.string.generate_image),
                checked = ingredient.generationEnabled,
                onCheckedChange = onIaGenerationChanged,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = ingredient.generationEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppTextField(
                        enabled = ingredient.generationEnabled,
                        value = ingredient.prompt ?: "",
                        label = { Text(stringResource(R.string.custom_prompt)) },
                        onValueChange = onPromptChanged,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = onGeneratedImageReset,
                        enabled = !ingredient.imageUrl.isNullOrBlank()
                    ) {
                        Text(stringResource(R.string.reset_image))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientImageGrid(
    text: String,
    modifier: Modifier = Modifier,
    onImageSelected: (name: DefaultIngredients?) -> Unit = {},
) {

    val values = LocalStepCardValues.current
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(values.iconSize),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            Box {
                Avatar(
                    Avatar.Image(text), Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        ) {
                            onImageSelected(null)
                        }
                )
            }
        }

        items(DefaultIngredients.entries) {
            Box {
                Avatar(
                    Avatar.StaticImage(it.drawable), Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        ) {
                            onImageSelected(it)
                        }
                )
            }
        }
    }
}

@Composable
@Preview
fun UpdateIngredientPreview() {
    AppTheme {
        UpdateIngredient(
            SimpleIngredient(
                id = 0,
                name = "Test Ingredient",
                imageUrl = null,
                removable = false
            ),
            isIaGenerationEnabled = true
        )
    }
}

@Composable
@Preview
fun IngredientImageGridPreview() {
    AppTheme(darkTheme = false) {
        Surface(modifier = Modifier.size(172.dp)) {
            IngredientImageGrid("T")
        }
    }
}
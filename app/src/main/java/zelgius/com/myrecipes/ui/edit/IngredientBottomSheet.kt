@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.IngredientRepository
import zelgius.com.myrecipes.data.abrv
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.string
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.AppDropDown
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import java.text.DecimalFormat
import javax.inject.Inject

@Composable
fun IngredientBottomSheet(
    modalBottomSheetState: SheetState,
    initialIngredient: Ingredient?,
    viewModel: AddIngredientViewModel = hiltViewModel<AddIngredientViewModel>(),
    onSaved: (Ingredient) -> Unit = {},
    onDismiss: () -> Unit = {}
) {

    LaunchedEffect(initialIngredient) {
        viewModel.init(initialIngredient)
    }

    val ingredient by viewModel.ingredientFlow.collectAsState(
        null
    )
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())

    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = onDismiss) {

        if (ingredients.isNotEmpty() && ingredient != null) IngredientBottomSheet(ingredient = ingredient!!,
            ingredients = ingredients,
            onSaved = {
                viewModel.ingredient?.let(onSaved)
            },
            onChanged = {
                viewModel.setIngredient(it)
            })
    }
}

val decimalFormat = DecimalFormat("#.##")

@Composable
private fun IngredientBottomSheet(
    ingredient: Ingredient,
    ingredients: List<Ingredient>,
    onChanged: (Ingredient) -> Unit,
    onSaved: () -> Unit = {},
) {

    var quantity: String by remember {
        mutableStateOf(if (ingredient.quantity > 0) decimalFormat.format(ingredient.quantity) else "")
    }
    var isQuantityError by remember {
        mutableStateOf(false)
    }
    var isNameError by remember {
        mutableStateOf(false)
    }

    Column(modifier = Modifier.padding(8.dp)) {
        EditIngredient(
            ingredients, onChanged, ingredient, isNameError
        )

        Row(modifier = Modifier.padding(top = 16.dp)) {
            AppTextField(
                quantity,
                onValueChange = { quantity = it },
                modifier = Modifier.weight(1f),
                isError = isQuantityError,
                label = { Text(stringResource(R.string.quantity)) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),

                )
            UnitDropdown(ingredient, onChanged)
        }

        Button(onClick = {
            val q = quantity.toDoubleOrNull()
            isQuantityError = q == null || q <= 0
            isNameError = ingredient.name.isBlank()

            if (!isQuantityError && !isNameError) {
                onChanged(ingredient.copy(quantity = q!!))
                onSaved()
            }
        }, modifier = Modifier
            .align(End)
            .padding(8.dp)) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
private fun EditIngredient(
    ingredients: List<Ingredient>,
    onChanged: (Ingredient) -> Unit,
    ingredient: Ingredient,
    isNameError: Boolean,
    modifier: Modifier = Modifier,
) {
    var newIngredient by remember {
        mutableStateOf(false)
    }

    Row(modifier = modifier) {
        AnimatedContent(
            newIngredient, label = "ingredient_container",
            modifier = Modifier.weight(1f),
        ) { showTextField ->

            if (!showTextField) {
                IngredientDropdown(ingredients, onChanged, ingredient, onNewIngredient = {
                    newIngredient = true
                    onChanged(ingredient.copy(id = null, name = "", imageUrl = null))
                })
            } else AppTextField(
                ingredient.name,
                onValueChange = { onChanged(ingredient.copy(name = it)) },
                modifier = Modifier
                    .weight(1f),
                isError = isNameError,
                maxLines = 1,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            newIngredient = false
                        },
                    ) {
                        Icon(imageVector = Icons.TwoTone.Close, contentDescription = "")
                    }
                },
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Composable
private fun UnitDropdown(
    ingredient: Ingredient, onChanged: (Ingredient) -> Unit
) {
    AppDropDown(modifier = Modifier
        .defaultMinSize(150.dp)
        .padding(start = 16.dp), selection = {
        Box(contentAlignment = Center) {
            AppTextField(
                value = " ",
                modifier = Modifier.focusProperties { canFocus = false },
                onValueChange = {},
                label = { Text(stringResource(R.string.unit)) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                readOnly = true,
            )
            Text(
                ingredient.unit.let {
                    if (it == Ingredient.Unit.Unit) stringResource(R.string.unit)
                    else it.abrv(LocalContext.current)
                }, modifier = Modifier
                    .align(CenterStart)
                    .padding(start = 16.dp, end = 30.dp)
            )
            Icon(
                Icons.TwoTone.KeyboardArrowDown,
                contentDescription = "",
                modifier = Modifier
                    .align(
                        CenterEnd
                    )
                    .padding(end = 4.dp)
            )
        }
    }, item = {
        Text(it.string(LocalContext.current))
    }, items = Ingredient.Unit.entries, onItemSelected = {
        onChanged(ingredient.copy(unit = it))
    }, selectedItem = ingredient.unit
    )
}

@Composable
private fun IngredientDropdown(
    ingredients: List<Ingredient>,
    onChanged: (Ingredient) -> Unit,
    ingredient: Ingredient,
    shape: RoundedCornerShape = CircleShape,
    onNewIngredient: () -> Unit = {}
) {
    AppDropDown(modifier = Modifier.width(IntrinsicSize.Max), selection = {
        OutlinedTextField(value = " ",
            shape = shape,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.ingredient)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            leadingIcon = {
                if (it != null) Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = CenterVertically
                ) {
                    Ingredient(
                        it, text = it.name, modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.TwoTone.KeyboardArrowDown, contentDescription = "")
                }
            })
    }, item = {
        if (it != null) Ingredient(it, text = it.name)
        else Text(stringResource(R.string.new_ingredient))
    }, items = listOf(null) + ingredients, onItemSelected = {

        if (it != null) {
            onChanged(it)
        } else {
            onChanged(ingredient.copy(name = ""))
            onNewIngredient()
        }
    }, selectedItem = ingredient
    )
}


@Preview
@Composable
fun IngredientBottomSheetPreview() {
    val recipe = createDummyModel()

    var ingredient by remember {
        mutableStateOf(
            recipe.ingredients.first().copy(
                unit = Ingredient.Unit.TableSpoon, quantity = 500000.0
            )
        )
    }
    MaterialTheme {
        Scaffold {
            Column(Modifier.padding(it)) {
                IngredientBottomSheet(
                    ingredient = ingredient,
                    ingredients = recipe.ingredients,
                    onChanged = {
                        ingredient = it
                    })
            }
        }
    }
}

@HiltViewModel
class AddIngredientViewModel @Inject constructor(
    ingredientRepository: IngredientRepository,
    private val dataStoreRepository: DataStoreRepository,
) : ViewModel() {
    private val _ingredientFlow = MutableStateFlow<Ingredient?>(null)

    val ingredient get() = _ingredientFlow.value

    val ingredientFlow = _ingredientFlow.asStateFlow().filterNotNull()

    val ingredientsFlow = ingredientRepository.getFlow()

    fun init(ingredient: Ingredient?) {
        if (ingredient == null) {
            viewModelScope.launch {
                _ingredientFlow.value = ingredientsFlow.first().first()
            }
        } else _ingredientFlow.value = ingredient

    }

    fun setIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val unit: Ingredient.Unit =
                if (ingredient.name != _ingredientFlow.value?.name) dataStoreRepository.unit(
                    ingredient.name
                )?.let {
                    Ingredient.Unit.valueOf(it)
                } ?: _ingredientFlow.value?.unit ?: Ingredient.Unit.Unit
                else ingredient.unit

            _ingredientFlow.value = ingredient.copy(unit = unit)
        }

    }

}
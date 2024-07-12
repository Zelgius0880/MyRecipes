@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.entities.IngredientEntity
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.AppDropDown
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.recipe.Ingredient

@Composable
fun StepBottomSheet(
    modalBottomSheetState: SheetState,
    initialStep: Step? = null,
    recipe: Recipe,
    viewModel: StepBottomSheetViewModel = hiltViewModel(creationCallback =
    { factory: StepBottomSheetViewModel.Factory ->
        factory.create(initialStep, recipe)
    }),
    onSaved: (Step) -> Unit,
    onDismiss: () -> Unit = {}
) {

    val step by viewModel.stepFlow.collectAsState()
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())
    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = onDismiss) {
        StepBottomSheet(
            step = step,
            ingredients = ingredients,
            onSaved = onSaved,
            onTextChange = viewModel::onTextChange,
            onIngredientAdded = viewModel::onIngredientAdded,
            onIngredientRemoved = viewModel::onIngredientRemoved
        )
    }
}

@Composable
private fun StepBottomSheet(
    step: Step,
    ingredients: List<Ingredient>,
    onTextChange: (String) -> Unit,
    onIngredientAdded: (Ingredient) -> Unit,
    onIngredientRemoved: (Ingredient) -> Unit,
    onSaved: (Step) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Text(
            text = stringResource(R.string.ingredients),
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleSmall
        )
        LazyColumn {
            items(step.ingredients, key = { it }) {
                Row(modifier = Modifier.animateItem()) {
                    Ingredient(it, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onIngredientRemoved(it) }) {
                        Icon(
                            imageVector = Icons.TwoTone.Close,
                            contentDescription = ""
                        )
                    }
                }
            }
        }
        AddIngredient(ingredients, onIngredientAdded)

        AppTextField(
            step.text, onValueChange = onTextChange,
            label = {
                Text(text = stringResource(R.string.step_description))
            },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RectangleShape
        )

        Button(
            onClick = { onSaved(step) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}

@Composable
private fun AddIngredient(ingredients: List<Ingredient>, onClicked: (Ingredient) -> Unit) {
    AppDropDown(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        selection = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {},
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = stringResource(R.string.select_an_ingredient))
                }
            }
        },
        item = {
            Ingredient(it)
        },
        items = ingredients,
        onItemSelected = onClicked,
        selectedItem = null
    )

}

@Preview
@Composable
fun StepBottomSheetContentPreview() {
    val recipe = createDummyModel()

    val viewModel = StepBottomSheetViewModel(
        recipe.steps.last(), recipe
    )
    val step by viewModel.stepFlow.collectAsState()
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())

    Scaffold {
        Box(modifier = Modifier.padding(it))
        StepBottomSheet(
            step,
            ingredients,
            onTextChange = viewModel::onTextChange,
            onIngredientAdded = viewModel::onIngredientAdded,
            onIngredientRemoved = viewModel::onIngredientRemoved
        ) {}

    }
}

@Preview
@Composable
fun StepBottomSheetPreview() {
    val recipe = createDummyModel().let {
        it.copy(ingredients = it.ingredients + List(10) { i ->
            Ingredient(
                name = "Ingredient $i", recipe = it, quantity = 200.0,
                unit = Ingredient.Unit.Gramme,
                imageUrl = null,
                optional = i % 2 == 0,
                sortOrder = i+100,
                id = 100 + i.toLong(),
                step = null,
            )
        })
    }

    val viewModel = StepBottomSheetViewModel(
        recipe.steps.last(), recipe
    )
    val sheetState = rememberModalBottomSheetState()
    var openBottomSheet by remember {
        mutableStateOf(true)
    }
    Surface(modifier = Modifier.fillMaxSize()) {

        TextButton(onClick = {
            openBottomSheet = true
        }) {
            Text("Show Modal")
        }

        if (openBottomSheet) {
            StepBottomSheet(
                sheetState,
                recipe.steps.last(),
                recipe,
                viewModel,
                onSaved = {}
            ) {
                openBottomSheet = false
            }
        }

    }
}

@HiltViewModel(assistedFactory = StepBottomSheetViewModel.Factory::class)
class StepBottomSheetViewModel @AssistedInject constructor(
    @Assisted private val step: Step?,
    @Assisted private val recipe: Recipe,
) : ViewModel() {

    private val _stepFlow = MutableStateFlow(step ?: Step(text = "", recipe = recipe))
    val stepFlow = _stepFlow.asStateFlow()

    val ingredientsFlow = stepFlow.map { step ->
        recipe.ingredients.filterNot { it in step.ingredients }
    }

    fun onTextChange(text: String) {
        _stepFlow.value = _stepFlow.value.copy(text = text)
    }

    fun onIngredientAdded(ingredient: Ingredient) {
        _stepFlow.value =
            _stepFlow.value.copy(ingredients = _stepFlow.value.ingredients + ingredient)
    }

    fun onIngredientRemoved(ingredient: Ingredient) {
        _stepFlow.value =
            _stepFlow.value.copy(ingredients = _stepFlow.value.ingredients - ingredient)
    }

    @AssistedFactory
    interface Factory : ViewModelProvider.Factory {
        fun create(ingredient: Step?, recipe: Recipe): StepBottomSheetViewModel
    }
}
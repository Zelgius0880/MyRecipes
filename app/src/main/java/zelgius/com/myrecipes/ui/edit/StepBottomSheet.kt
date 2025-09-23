@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.model.Step
import zelgius.com.myrecipes.ui.common.AppDropDown
import zelgius.com.myrecipes.ui.common.AppLabeledCheckbox
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.recipe.Ingredient
import zelgius.com.myrecipes.ui.edit.viewModel.StepItem
import zelgius.com.myrecipes.ui.preview.createDummyModel

@Composable
fun StepBottomSheet(
    modalBottomSheetState: SheetState,
    initialStep: StepItem? = null,
    recipe: Recipe,
    viewModel: StepBottomSheetViewModel = StepBottomSheetViewModel(),
    onSaved: (StepItem) -> Unit,
    onDismiss: () -> Unit = {}
) {

    LaunchedEffect(Unit) {
        viewModel.init(initialStep, recipe)
    }

    val step by viewModel.stepFlow.collectAsState()
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())
    val stepIngredients by viewModel.stepIngredientFlow.collectAsState(emptyList())
    ModalBottomSheet(sheetState = modalBottomSheetState, onDismissRequest = onDismiss) {
        StepBottomSheet(
            item = step,
            ingredients = ingredients,
            stepIngredients = stepIngredients,
            onSaved = onSaved,
            onTextChange = viewModel::onTextChange,
            onIngredientAdded = viewModel::onIngredientAdded,
            onIngredientRemoved = viewModel::onIngredientRemoved,
            onOptionalChanged = viewModel::onOptionalChanged

        )
    }
}

@Composable
private fun StepBottomSheet(
    item: StepItem,
    stepIngredients: List<Ingredient>,
    ingredients: List<Ingredient>,
    onTextChange: (String) -> Unit,
    onIngredientAdded: (Ingredient) -> Unit,
    onIngredientRemoved: (Ingredient) -> Unit,
    onOptionalChanged: (Boolean) -> Unit = {},
    onSaved: (StepItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {

        if (stepIngredients.isNotEmpty() || ingredients.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ingredients),
                modifier = Modifier.padding(vertical = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            val listState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .heightIn(0.dp, 300.dp)
                    .padding(vertical = 8.dp)
            ) {
                items(stepIngredients, key = { it }) {
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

                if (ingredients.isNotEmpty())
                    item {
                        IngredientEdition(ingredients) {
                            onIngredientAdded(it)
                            scope.launch {
                                listState.animateScrollToItem(stepIngredients.size)
                            }
                        }
                    }
            }
        }

        AppTextField(
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Text
            ),
            value = item.step.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.step_description))
            },
            maxLines = 4,
        )

        Row {
            AppLabeledCheckbox(
                label = stringResource(R.string.optional),
                checked = item.step.optional == true,
                onCheckedChange = onOptionalChanged
            )
            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onSaved(item) },
                modifier = Modifier
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(id = R.string.save))
            }
        }

    }
}

@Composable
private fun IngredientEdition(ingredients: List<Ingredient>, onClicked: (Ingredient) -> Unit) {
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

    val viewModel = StepBottomSheetViewModel().apply {
        init(StepItem(recipe.steps.last()), recipe)
    }
    val step by viewModel.stepFlow.collectAsState()
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())

    Scaffold {
        Box(modifier = Modifier.padding(it))
        StepBottomSheet(
            step,
            ingredients,
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
                sortOrder = i + 100,
                id = 100 + i.toLong(),
                idIngredient = null,
                step = null,
            )
        })
    }

    val viewModel = StepBottomSheetViewModel(
    ).apply {
        init(StepItem(recipe.steps.last()), recipe)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                StepItem(recipe.steps.last(), recipe.ingredients),
                recipe,
                viewModel,
                onSaved = {}
            ) {
                openBottomSheet = false
            }
        }

    }
}

class StepBottomSheetViewModel : ViewModel() {

    private var recipe: Recipe? = null
    private val _stepFlow = MutableStateFlow(StepItem(Step(text = "", recipe = recipe)))
    val stepFlow = _stepFlow.asStateFlow()

    val stepIngredientFlow get() = _stepFlow.map { it.ingredients }

    val ingredientsFlow = stepFlow.map { step ->
        recipe?.ingredients?.filter { i -> i.step == null && i !in step.ingredients } ?: emptyList()
    }


    fun init(item: StepItem?, recipe: Recipe) {
        this.recipe = recipe
        _stepFlow.value = item ?: StepItem(Step(text = "", recipe = recipe))
    }

    fun onTextChange(text: String) {
        val item = _stepFlow.value
        _stepFlow.value = item.copy(step = item.step.copy(text = text))
    }

    fun onIngredientAdded(ingredient: Ingredient) {
        val item = _stepFlow.value
        _stepFlow.value = item.copy(ingredients = item.ingredients + ingredient)
    }

    fun onIngredientRemoved(ingredient: Ingredient) {
        val item = _stepFlow.value

        val index = recipe?.ingredients?.indexOf(ingredient)?.takeIf { it >= 0 }
        if (index != null) {
            val ingredients = recipe?.ingredients?.toMutableList() ?: return
            ingredients[index] = ingredient.copy(step = null)
            recipe = recipe?.copy(ingredients = ingredients)
        }

        _stepFlow.value = item.copy(ingredients = item.ingredients - ingredient)
    }

    fun onOptionalChanged(optional: Boolean) {
        val item = _stepFlow.value
        _stepFlow.value = item.copy(step = item.step.copy(optional = optional))
    }
}
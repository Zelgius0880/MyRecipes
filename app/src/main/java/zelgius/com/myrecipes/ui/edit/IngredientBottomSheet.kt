@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.edit

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.R
import zelgius.com.myrecipes.data.IngredientRepository
import zelgius.com.myrecipes.data.abrv
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.string
import zelgius.com.myrecipes.preview.createDummyModel
import zelgius.com.myrecipes.ui.common.AppDropDown
import zelgius.com.myrecipes.ui.common.AppTextField
import zelgius.com.myrecipes.ui.common.recipe.Ingredient

@Composable
fun IngredientBottomSheet(
    modalBottomSheetState: SheetState,
    initialIngredient: Ingredient? = null,
    viewModel: IngredientBottomSheetViewModel = hiltViewModel(creationCallback =
    { factory: IngredientBottomSheetViewModel.Factory ->
        factory.create(initialIngredient)
    }),
    onSaved: (Ingredient) -> Unit
) {

    val ingredient by viewModel.ingredientFlow.collectAsState()
    val ingredients by viewModel.ingredientsFlow.collectAsState(emptyList())

    IngredientBottomSheetView(ingredient, ingredients) {
    }
}

@Composable
private fun IngredientBottomSheetView(
    ingredient: Ingredient? = null,
    ingredients: List<Ingredient>,
    onSaved: (Ingredient) -> Unit,
) {
    AlertDialog(onDismissRequest = {}, confirmButton = {
        Button(onClick = { ingredient?.let { onSaved(it) } }) {
            Text(stringResource(R.string.save))
        }
    }, dismissButton = {}, text = {
        Row(modifier = Modifier.fillMaxWidth()) {
            AppDropDown(
                modifier = Modifier.weight(1f),
                selection = {
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(
                            topStartPercent = 50,
                            bottomStartPercent = 50,
                            bottomEndPercent = 0,
                            topEndPercent = 0
                        ), modifier = Modifier.fillMaxWidth()
                    ) {
                        if (it != null)
                            Ingredient(
                                it,
                                text = it.name,
                            )
                    }
                },
                item = {
                    Ingredient(it, text = it.name)
                },
                items = ingredients,
                onItemSelected = {

                },
                selectedItem = ingredient
            )

            AppTextField(
                "${ingredient?.quantity?: ""}", onValueChange = {},
                modifier = Modifier
                    .height(36.dp)
                    .defaultMinSize(50.dp)
                    .width(IntrinsicSize.Min),
                shape = RectangleShape
            )
            AppDropDown(
                modifier = Modifier.weight(1f),
                selection = {
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(
                            topStartPercent = 0,
                            bottomStartPercent = 0,
                            bottomEndPercent = 50,
                            topEndPercent = 50
                        ),
                        modifier = Modifier.fillMaxWidth()

                    ) {
                        Text(
                            (ingredient?.unit ?: Ingredient.Unit.Unit).abrv(LocalContext.current),
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                item = {
                    Text(it.string(LocalContext.current))
                },
                items = Ingredient.Unit.entries,
                onItemSelected = {

                },
                selectedItem = ingredient?.unit ?: Ingredient.Unit.Unit
            )

        }
    })
}

@Preview
@Composable
fun IngredientBottomSheetPreview() {
    val recipe = createDummyModel()

    IngredientBottomSheetView(recipe.ingredients.first().copy(
        unit = Ingredient.Unit.TableSpoon,
        quantity = 500000.0
    ), recipe.ingredients) {}
}

@HiltViewModel(assistedFactory = IngredientBottomSheetViewModel.Factory::class)
class IngredientBottomSheetViewModel @AssistedInject constructor(
    @Assisted private val ingredient: Ingredient?,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _ingredientFlow = MutableStateFlow(ingredient)
    val ingredientFlow = _ingredientFlow.asStateFlow()

    val ingredientsFlow = ingredientRepository.getFlow()

    @AssistedFactory
    interface Factory : ViewModelProvider.Factory {
        fun create(ingredient: Ingredient?): IngredientBottomSheetViewModel
    }
}
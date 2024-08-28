@file:OptIn(ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun <T> AppDropDown(
    items: List<T>,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    onItemSelected: (T) -> Unit,
    selection: @Composable ExposedDropdownMenuBoxScope.(T?) -> Unit = { DefaultSelectedValue(it) },
    item: @Composable (T) -> Unit = { Text("$it") }
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(selectedItem) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = {
            expanded = it
        }
    ) {
        Box(modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)) {
            selection(selectedOption)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            matchTextFieldWidth = false,
            onDismissRequest = {
                expanded = false
            }
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        selectedOption = selectionOption
                        expanded = false
                        onItemSelected(selectionOption)
                    },
                    text = {
                        item(selectionOption)
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> ExposedDropdownMenuBoxScope.DefaultSelectedValue(
    selectedOption: T?,
) {
    OutlinedTextField(
        readOnly = true,
        value = selectedOption?.let { "$selectedOption" } ?: "",
        onValueChange = { },
        label = { Text("Label") },
        shape = RoundedCornerShape(50),
        trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(
                expanded = true
            )
        },
    )
}

@Preview
@Composable
private fun AppDropDownPreview() {
    Scaffold {
        AppDropDown(
            items = listOf("One", "Two", "Three"),
            modifier = Modifier.padding(it),
            selectedItem = "One",
            onItemSelected = {

            })
    }

}
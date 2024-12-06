@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package zelgius.com.myrecipes.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    shape: Shape = RoundedCornerShape(24.dp),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    label: @Composable () -> Unit = {},
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textAlign: TextAlign = TextAlign.Start,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 4.dp
    )
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        maxLines = maxLines,
        singleLine = singleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        )
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            label = label,
            isError = isError,
            visualTransformation = visualTransformation,
            innerTextField = innerTextField,
            singleLine = singleLine,
            enabled = enabled,
            suffix = trailingIcon,
            prefix = leadingIcon,
            colors = OutlinedTextFieldDefaults.colors(cursorColor = MaterialTheme.colorScheme.onSurfaceVariant),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(cursorColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    interactionSource = interactionSource,
                    shape = shape,
                )
            },
            interactionSource = interactionSource,
            contentPadding = contentPadding, // this is how you can remove the padding
        )
    }
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    label: @Composable () -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textAlign: TextAlign = TextAlign.Start
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign
        ),
        singleLine = singleLine,
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value.text,
            label = label,
            isError = isError,
            visualTransformation = visualTransformation,
            innerTextField = innerTextField,
            singleLine = singleLine,
            enabled = enabled,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(cursorColor = MaterialTheme.colorScheme.onSurfaceVariant),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(cursorColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(50),
                )
            },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ), // this is how you can remove the padding
        )
    }
}
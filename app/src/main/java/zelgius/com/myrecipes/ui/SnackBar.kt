package zelgius.com.myrecipes.ui

import androidx.compose.material3.SnackbarDuration

data class SnackBar(
    val message: String,
    val action: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionListener: () -> Unit = {},
)
package zelgius.com.myrecipes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val lightThemeColors = lightColorScheme(
    primary = Color(0xFF1976d2),
    secondary = Color(0xFFff9800),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

val darkThemeColors = darkColorScheme(
    primary = Color(0xFF1976d2),
    secondary = Color(0xFFFF5722),
    background = Color(0xFF161616),
    surface = Color(0xFF21252E),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)


@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkThemeColors
        } else {
            lightThemeColors
        },
        shapes = Shapes()
    ) {
        content()
    }
}
package zelgius.com.myrecipes.compose

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val lightThemeColors = lightColors(
    primary = Color(0xFF1976d2),
    primaryVariant = Color(0xFF63a4ff),
    secondary = Color(0xFFff9800),
    secondaryVariant = Color(0xFFFFC947),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

val darkThemeColors = darkColors(
    primary = Color(0xFF1976d2),
    primaryVariant = Color(0xFF63a4ff),
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

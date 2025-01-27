package life.sochpekharoch.serenity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFF1F1),    // Changed to light pink
    onPrimary = Color.Black,        // Changed to black for contrast on pink
    secondary = Color.Black,        // Changed to black
    onSecondary = Color.White,      // White text on black background
    tertiary = Color.Gray,          // Third color in your scheme
    background = Color.White,       // Background color
    surface = Color.White,          // Surface color for cards, sheets, menus
    onSurface = Color.Black,        // Color used on top of surface
    error = Color.Red,              // Color used for errors
    onError = Color.White           // Color used on top of error color
)

@Composable
fun SerenityTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
package agnello.vinicola.appvinicolla.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val CustomColorScheme = lightColorScheme(
    primary = Color(0xFF7E201B),
    onPrimary = Color.Red,
    secondary = Color(0xFF070111),
    onSecondary = Color.Black,
)

@Composable
fun VinicolaAgnelloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content
    )
}

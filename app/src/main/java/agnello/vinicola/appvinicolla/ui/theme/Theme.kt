package agnello.vinicola.appvinicolla.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val CustomColorScheme = lightColorScheme(
    primary = Color(0xFF893939),
    onPrimary = Color.Red,
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color.Black,
    background = Color(0xFF1C1C1C)

)

@Composable
fun VinicolaAgnelloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content,
    )
}

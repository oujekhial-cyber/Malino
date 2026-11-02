package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemedAddAccountButtonGuardTest {
    @Test fun `add account call to action follows active material theme`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
        assertTrue(source.contains("Brush.horizontalGradient"))
        assertTrue(source.contains("MaterialTheme.colorScheme.primary.copy(alpha = .96f)"))
        assertTrue(source.contains("MaterialTheme.colorScheme.tertiary.copy(alpha = .88f)"))
        assertTrue(source.contains("Icons.Filled.Add"))
        assertTrue(source.contains("حساب بانکی یا صندوق نقدی"))
        assertTrue(source.contains(".clickable { nav.navigate(\"accountEdit/0\") }"))
    }
}

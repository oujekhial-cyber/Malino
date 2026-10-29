package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class NotificationDarkThemeContrastGuardTest{@Test fun semantic(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/NotificationCenterScreen.kt").readText();assertTrue(s.contains("MaterialTheme.colorScheme.background"));assertTrue(s.contains("MaterialTheme.colorScheme.primaryContainer"));assertTrue(s.contains("Brush.horizontalGradient"))}}

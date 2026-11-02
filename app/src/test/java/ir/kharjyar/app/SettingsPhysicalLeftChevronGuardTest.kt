package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class SettingsPhysicalLeftChevronGuardTest{
 @Test fun settingsNavigationUsesNonMirroredLeftVector(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  assertTrue(s.contains("import androidx.compose.material.icons.filled.ChevronLeft"))
  assertTrue(s.contains("Icons.Filled.ChevronLeft"))
  assertFalse("Bidi-mirrored text chevrons must not return",s.contains("Text(\"›\"")||s.contains("Text(\"‹\""))
  assertFalse("AutoMirrored would point right in RTL",s.contains("AutoMirrored.Filled.ChevronLeft"))
 }
}

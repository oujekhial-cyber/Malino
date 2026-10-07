package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemedManagementFabGuardTest {
 @Test fun `requested management pages use skin colored add buttons`() {
  val component=File("src/main/java/ir/kharjyar/app/ui/components/ThemedFab.kt").readText()
  assertTrue(component.contains("LocalAppSkin.current")&&component.contains("skin.fabGradient.firstOrNull()"))
  assertTrue(component.contains("background.luminance()")&&component.contains("contentColor=foreground"))
  listOf("DebtsScreen.kt","AssetsScreen.kt","RemindersScreen.kt","CivicCenterScreen.kt","VehiclesScreen.kt").forEach{name->
   val source=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText()
   assertTrue(name,source.contains("ThemedFloatingActionButton"))
   assertFalse(name,Regex("(?<!Themed)FloatingActionButton\\(").containsMatchIn(source))
  }
  val civic=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText()
  assertTrue(civic.contains("ThemedExtendedFloatingActionButton"))
 }
}

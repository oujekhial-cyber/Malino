package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDarkThemeContrastGuardTest {
 @Test fun `notification center uses semantic foregrounds and bright dark accents`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/NotificationCenterScreen.kt").readText()
  listOf(
   "contentColor=MaterialTheme.colorScheme.onSurface",
   "color=MaterialTheme.colorScheme.onSurface",
   "tint=MaterialTheme.colorScheme.onPrimaryContainer",
   "MaterialTheme.colorScheme.background.luminance()<.5f",
   "darkAccent=Color(0xFF82BDFF)",
   "darkAccent=Color(0xFFB4AEFF)",
   "darkAccent=Color(0xFFFF91A3)",
   "contentColor=if(dark)Color(0xFF151318) else Color.White"
  ).forEach{assertTrue(it,screen.contains(it))}
 }
}

package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class SakuraLightThemeGuardTest {
 @Test fun `sakura follows app brightness mode and has a real light skin`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt").readText()
  listOf("SakuraLightScheme = lightColorScheme(","SakuraLightSkin = SakuraSkin.copy(","dark = false","Palette.SAKURA -> if (wantsDark) SakuraScheme else SakuraLightScheme","effectivePalette == Palette.SAKURA && !wantsDark").forEach{assertTrue(it,source.contains(it))}
 }
}

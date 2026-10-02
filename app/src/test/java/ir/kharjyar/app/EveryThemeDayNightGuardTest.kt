package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class EveryThemeDayNightGuardTest{
 @Test fun everyVisualFamilyFollowsBrightnessMode(){
  val s=File("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt").readText()
  listOf("VioletDarkScheme = darkColorScheme(","VioletDarkSkin=VioletSkin.copy(","LotusLightScheme = lightColorScheme(","LotusLightSkin=LotusSkin.copy(","OceanLightScheme = lightColorScheme(","OceanLightSkin=OceanSkin.copy(","GoldLightScheme = lightColorScheme(","GoldLightSkin=GoldSkin.copy(").forEach{assertTrue(it,s.contains(it))}
  listOf("Palette.VIOLET -> if(wantsDark) VioletDarkSkin else VioletSkin","Palette.LOTUS -> if(wantsDark) LotusSkin else LotusLightSkin","Palette.OCEAN -> if(wantsDark) OceanSkin else OceanLightSkin","Palette.GOLD -> if(wantsDark) GoldSkin else GoldLightSkin").forEach{assertTrue(it,s.contains(it))}
  assertTrue(s.contains("ThemeMode.SYSTEM -> isSystemInDarkTheme()"))
 }
}

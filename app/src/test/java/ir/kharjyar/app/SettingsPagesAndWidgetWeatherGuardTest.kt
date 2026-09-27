package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class SettingsPagesAndWidgetWeatherGuardTest {
 @Test fun `settings cards navigate to dedicated pages`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText();val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();listOf("appearance","account","numbers","permissions","security","data","privacy","about").forEach{assertTrue(s.contains("settings/$it"))};assertTrue(root.contains("settings/{section}"))}
 @Test fun `split widget nests weather beneath date`(){val x=File("src/main/res/layout/w_split.xml").readText();val weather=x.indexOf("@+id/w_weather");val gregorian=x.indexOf("@+id/w_gregorian");assertTrue(weather>gregorian);assertTrue(x.substring(weather).contains("android:drawablePadding=\"2dp\""))}
}

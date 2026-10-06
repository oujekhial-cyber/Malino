package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class WidgetSplitDefaultIntegrationGuardTest {
 @Test fun `launcher placeholder and runtime settings both default to split`() {
  val xml=File("src/main/res/xml/widget_info.xml").readText()
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  assertTrue(xml.contains("android:initialLayout=\"@layout/w_split\""))
  assertTrue(prefs.contains("enumOf(p[Keys.W_LAYOUT], WidgetLayout.SPLIT)"))
  assertFalse(root.contains("setWidgetLayout(WidgetLayout.ROYAL)"))
 }
 @Test fun `split widget app name stays on physical right`() {
  val layout=File("src/main/res/layout/w_split.xml").readText()
  val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
  val title=layout.substringAfter("android:id=\"@+id/w_title\"").substringBefore("/>")
  assertTrue(title.contains("android:layout_width=\"match_parent\"")&&title.contains("android:gravity=\"right\""))
  assertTrue(widget.contains("views.setInt(R.id.w_app_area,\"setGravity\",titleGravity)"))
  assertTrue(widget.contains("views.setInt(R.id.w_clock_area,\"setGravity\",clockGravity)"))
 }
 @Test fun `legacy auto pin royal default migrates without blocking later user choice`() {
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  assertTrue(prefs.contains("WIDGET_SPLIT_DEFAULT_MIGRATED"))
  assertTrue(prefs.contains("p[Keys.W_LAYOUT] == WidgetLayout.ROYAL.name"))
  assertTrue(prefs.contains("it[Keys.WIDGET_SPLIT_DEFAULT_MIGRATED] = true"))
 }
}

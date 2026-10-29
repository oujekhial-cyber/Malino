package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetTouchPlacementAndLinesGuardTest {
 @Test fun `settings preview supports touch placement with snap grid and attached lines`() {
  val settings=File("src/main/java/ir/kharjyar/app/ui/screens/WidgetSettingsScreen.kt").readText()
  val preview=File("src/main/java/ir/kharjyar/app/ui/components/WidgetPreview.kt").readText()
  val widget=File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
  assertTrue(settings.contains("چیدمان لمسی ویجت")&&settings.contains("editable=true"))
  assertTrue(settings.contains("onTitlePlaced")&&settings.contains("onClockPlaced"))
  assertTrue(preview.contains("PlacementGrid")&&preview.contains("detectDragGestures"))
  assertTrue(preview.contains("خرج‌یار و خط")&&preview.contains("قیمت‌ها")&&preview.contains("ساعت و تاریخ"))
  assertTrue(settings.contains("onValuesPlaced")&&settings.contains("setWidgetValuesAlign"))
  assertTrue(widget.contains("R.id.w_app_area")&&widget.contains("R.id.w_clock_area")&&widget.contains("R.id.w_row_1"))
  assertTrue(widget.contains("خط عنوان داخل w_app_area"))
 }
}

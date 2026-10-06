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
  assertTrue(preview.contains("نام، مبالغ و خط")&&preview.contains("ساعت، تاریخ و خط"))
  assertTrue(widget.contains("R.id.w_app_area")&&widget.contains("R.id.w_clock_area"))
  assertTrue(widget.contains("خط عنوان داخل w_app_area"))
 }
}

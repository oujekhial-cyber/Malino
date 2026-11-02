package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianWheelDatePickerGuardTest {
 @Test fun `all Persian date fields use independent cylindrical day month year wheels`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/components/PersianDatePicker.kt").readText()
  listOf(
   "fun PersianWheelDatePickerDialog(",
   "WheelPicker(1..maxDay, day",
   "WheelPicker(1..12, month",
   "WheelPicker(yearRange, year",
   "PersianDate.MONTH_NAMES[value - 1]",
   "val maxDay = PersianDate.monthLength(year, month)",
   "if (day > maxDay) day = maxDay",
   "labelOf: (Int) -> String",
   "if (showDate) {\n        PersianWheelDatePickerDialog(",
   "if (show) PersianWheelDatePickerDialog("
  ).forEach{assertTrue(it,source.contains(it))}
 }
}

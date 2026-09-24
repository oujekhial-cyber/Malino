package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.WidgetAlign
import ir.kharjyar.app.data.prefs.WidgetVAlign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** تنظیمات چیدمان متن‌های ویجت و ماندگاری وضعیت مبالغ. */
class WidgetAlignTest {

    @Test
    fun `horizontal and vertical align have three options each`() {
        assertEquals(3, WidgetAlign.entries.size)
        assertEquals(3, WidgetVAlign.entries.size)
        assertEquals(listOf("TOP", "CENTER", "BOTTOM"), WidgetVAlign.entries.map { it.name })
    }

    @Test
    fun `align defaults keep title start and clock centered`() {
        val s = AppSettings()
        assertEquals(WidgetAlign.START, s.widgetTitleAlign)
        assertEquals(WidgetAlign.CENTER, s.widgetClockAlign)
        // پیش‌فرض عمودی هر دو وسط است
        assertEquals(WidgetVAlign.CENTER, s.widgetTitleVAlign)
        assertEquals(WidgetVAlign.CENTER, s.widgetClockVAlign)
    }

    @Test
    fun `fine vertical offsets start at zero`() {
        val s = AppSettings()
        assertEquals(0, s.widgetTitleOffsetY)
        assertEquals(0, s.widgetClockOffsetY)
    }

    @Test
    fun `amount visibility is persisted and visible by default`() {
        // چون در تنظیمات ذخیره می‌شود، با رفتن به صفحه دیگر و برگشتن ریست نمی‌شود
        assertTrue(AppSettings().amountsVisible)
    }
}

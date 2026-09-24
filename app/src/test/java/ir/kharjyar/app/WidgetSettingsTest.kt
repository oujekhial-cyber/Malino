package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.WidgetLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** پیش‌فرض‌های ویجت و قالب‌های آماده. */
class WidgetSettingsTest {

    @Test
    fun `widget defaults are sane`() {
        val s = AppSettings()
        assertEquals(92, s.widgetOpacity)
        assertTrue(s.widgetShowNumbers)
        // پیش‌فرض: قالب دوبخشی
        assertEquals(WidgetLayout.SPLIT, s.widgetLayout)
    }

    @Test
    fun `six widget layouts are available`() {
        assertEquals(6, WidgetLayout.entries.size)
        assertEquals(
            listOf("ROYAL", "MINIMAL", "PANELS", "STACKED", "SPLIT", "GLASS"),
            WidgetLayout.entries.map { it.name }
        )
    }

    @Test
    fun `opacity maps to alpha fraction`() {
        assertEquals(0.92f, AppSettings().widgetOpacity / 100f, 0.0001f)
        assertEquals(0f, 0 / 100f, 0.0001f)
    }
}

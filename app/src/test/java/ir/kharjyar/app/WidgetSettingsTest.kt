package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** پیش‌فرض‌ها و محدوده‌های تنظیمات ظاهری ویجت. */
class WidgetSettingsTest {

    @Test
    fun `widget defaults are sane`() {
        val s = AppSettings()
        assertEquals(92, s.widgetOpacity)
        assertEquals(40, s.widgetClockSize)
        assertTrue(s.widgetShowClock)
        // ساعت باید از همه اجزای دیگر بزرگ‌تر باشد
        assertTrue(s.widgetClockSize > s.widgetDateSize)
        assertTrue(s.widgetClockSize > s.widgetValueSize)
        assertTrue(s.widgetValueSize > s.widgetLabelSize)
    }

    @Test
    fun `opacity maps to alpha fraction`() {
        assertEquals(0.92f, AppSettings().widgetOpacity / 100f, 0.0001f)
        assertEquals(0f, 0 / 100f, 0.0001f)
    }
}

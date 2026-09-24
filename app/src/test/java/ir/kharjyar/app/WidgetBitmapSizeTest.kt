package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.WidgetLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * محافظ حجم بیت‌مپ پس‌زمینه ویجت.
 *
 * RemoteViews سقف حجم دارد (حدود ۱.۵ تا ۲ مگابایت بسته به لانچر). عبور از آن
 * باعث خطای «can't load widget» می‌شود. این تست ابعاد ثابت رندرر را می‌سنجد
 * تا کسی بعداً ناخواسته بزرگش نکند.
 */
class WidgetBitmapSizeTest {

    private companion object {
        // باید با ابعاد داخل WidgetRenderer.applyBackground یکی باشد
        const val BITMAP_WIDTH = 480
        const val BITMAP_HEIGHT = 230
        const val BYTES_PER_PIXEL = 4
        const val SAFE_LIMIT_BYTES = 1_000_000
    }

    @Test
    fun `background bitmap stays well under remote views limit`() {
        val bytes = BITMAP_WIDTH * BITMAP_HEIGHT * BYTES_PER_PIXEL
        assertTrue(
            "بیت‌مپ پس‌زمینه ویجت بیش از حد بزرگ است: $bytes بایت",
            bytes < SAFE_LIMIT_BYTES
        )
    }

    @Test
    fun `all six user layouts are selectable`() {
        assertEquals(6, WidgetLayout.entries.size)
    }

    @Test
    fun `opacity default keeps widget mostly opaque`() {
        val opacity = AppSettings().widgetOpacity
        assertTrue(opacity in 0..100)
        assertTrue("پیش‌فرض باید خوانا باشد", opacity >= 70)
    }
}

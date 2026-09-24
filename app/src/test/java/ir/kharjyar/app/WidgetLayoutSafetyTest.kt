package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/**
 * نگهبان چیدمان‌های ویجت.
 *
 * RemoteViews فقط مجموعه محدودی از نماها را می‌پذیرد. استفاده از نمای غیرمجاز
 * (مثل View خام) باعث شکست inflate و خطای «can't load widget» در لانچر می‌شود.
 * این تست جلوی برگشتن آن اشتباه را می‌گیرد.
 */
class WidgetLayoutSafetyTest {

    /** نماهایی که RemoteViews رسماً پشتیبانی می‌کند. */
    private val allowed = setOf(
        "FrameLayout", "LinearLayout", "RelativeLayout", "GridLayout",
        "TextView", "ImageView", "Button", "ImageButton", "ProgressBar",
        "AnalogClock", "Chronometer", "TextClock", "ViewFlipper",
        "ListView", "GridView", "StackView", "AdapterViewFlipper"
    )

    private fun layoutFiles(): List<File> =
        File("src/main/res/layout").listFiles()
            ?.filter { it.name.startsWith("w_") && it.extension == "xml" }
            .orEmpty()

    @Test
    fun `widget layouts exist`() {
        assertTrue("چیدمان ویجت پیدا نشد", layoutFiles().isNotEmpty())
    }

    @Test
    fun `widget layouts use only RemoteViews-safe views`() {
        val tagPattern = Regex("""<([A-Za-z][A-Za-z0-9._]*)""")
        layoutFiles().forEach { file ->
            val text = file.readText()
            tagPattern.findAll(text).map { it.groupValues[1] }.forEach { tag ->
                // تگ‌های ساختاری XML نادیده گرفته می‌شوند
                if (tag == "xml" || tag.contains('.')) return@forEach
                assertTrue(
                    "نمای غیرمجاز «$tag» در ${file.name} — RemoteViews آن را inflate نمی‌کند",
                    tag in allowed
                )
            }
        }
    }

    @Test
    fun `no raw View tag remains`() {
        layoutFiles().forEach { file ->
            assertFalse(
                "تگ View خام در ${file.name} باقی مانده است",
                Regex("""<View\b""").containsMatchIn(file.readText())
            )
        }
    }
}

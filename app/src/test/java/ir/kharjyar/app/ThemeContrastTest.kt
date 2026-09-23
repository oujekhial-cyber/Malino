package ir.kharjyar.app

import androidx.compose.ui.graphics.Color
import ir.kharjyar.app.ui.theme.AllSkins
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * نگهبان خوانایی تم‌ها.
 *
 * تم روشن یک بار به این مشکل خورد که متن سفید روی کارت روشن نامرئی شد.
 * این تست نسبت کنتراست رنگ‌های کلیدی هر تم را طبق فرمول WCAG می‌سنجد تا
 * چنین حالتی دوباره از دست نرود.
 */
class ThemeContrastTest {

    /** روشنایی نسبی یک رنگ طبق WCAG 2.1 */
    private fun luminance(c: Color): Double {
        fun ch(v: Float): Double {
            val d = v.toDouble()
            return if (d <= 0.03928) d / 12.92 else ((d + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * ch(c.red) + 0.7152 * ch(c.green) + 0.0722 * ch(c.blue)
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    @Test
    fun `hero text is readable on its card gradient`() {
        AllSkins.forEach { skin ->
            skin.heroGradient.forEach { stop ->
                val r = contrast(skin.onHero, stop)
                assertTrue(
                    "متن کارت در تم «${skin.title}» خوانا نیست (نسبت ${"%.2f".format(r)})",
                    r >= 3.0
                )
            }
        }
    }

    @Test
    fun `body text is readable on background and cards`() {
        AllSkins.forEach { skin ->
            val onBg = contrast(skin.onBackdrop, skin.backgroundColors.first())
            assertTrue(
                "متن روی پس‌زمینه تم «${skin.title}» ضعیف است (${"%.2f".format(onBg)})",
                onBg >= 4.5
            )
            val onCard = contrast(skin.onBackdrop, skin.cardColor)
            assertTrue(
                "متن روی کارت تم «${skin.title}» ضعیف است (${"%.2f".format(onCard)})",
                onCard >= 4.5
            )
        }
    }

    @Test
    fun `income and expense colors stand out on cards`() {
        AllSkins.forEach { skin ->
            listOf("درآمد" to skin.incomeColor, "هزینه" to skin.expenseColor).forEach { (name, c) ->
                val r = contrast(c, skin.cardColor)
                assertTrue(
                    "رنگ $name در تم «${skin.title}» کم‌کنتراست است (${"%.2f".format(r)})",
                    r >= 3.0
                )
            }
        }
    }

    @Test
    fun `inactive nav icons remain visible`() {
        AllSkins.forEach { skin ->
            val r = contrast(skin.navUnselected, skin.navBarColor)
            assertTrue(
                "آیکون‌های غیرفعال نوار پایین در تم «${skin.title}» محوند (${"%.2f".format(r)})",
                r >= 3.0
            )
        }
    }
}

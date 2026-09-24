package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * نگهبان حریم خصوصی.
 *
 * وعده برنامه این است که آفلاین کار می‌کند و داده مالی جایی نمی‌رود.
 * این تست جلوی افزودن ناخواسته مجوزهای حساس را می‌گیرد.
 */
class PrivacyGuardTest {

    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun `app has no internet permission`() {
        assertFalse(
            "برنامه نباید مجوز اینترنت بگیرد؛ تحلیل جمله باید روی خود گوشی بماند",
            manifest.contains("android.permission.INTERNET")
        )
    }

    @Test
    fun `voice input does not require microphone permission`() {
        // ضبط صدا را برنامه سیستمی انجام می‌دهد، نه خرج‌یار
        assertFalse(
            "ورودی صوتی نباید به مجوز میکروفون نیاز داشته باشد",
            manifest.contains("android.permission.RECORD_AUDIO")
        )
        assertTrue(
            "برای دیدن برنامه گفتار به متن، queries لازم است",
            manifest.contains("android.speech.action.RECOGNIZE_SPEECH")
        )
    }

    @Test
    fun `parser runs fully on device`() {
        val parser =
            File("src/main/java/ir/kharjyar/app/core/nlp/TransactionParser.kt").readText()
        // هیچ فراخوانی شبکه‌ای نباید در پارسر باشد
        listOf("http://", "https://", "URL(", "OkHttp", "Retrofit").forEach { needle ->
            assertFalse("پارسر نباید به شبکه وصل شود ($needle)", parser.contains(needle))
        }
    }
}

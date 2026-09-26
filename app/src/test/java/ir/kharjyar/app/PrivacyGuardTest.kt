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
    fun `internet and coarse location are declared only for weather`() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.ACCESS_COARSE_LOCATION"))
        assertFalse("هواشناسی به موقعیت دقیق نیاز ندارد", manifest.contains("android.permission.ACCESS_FINE_LOCATION"))
        val weather = File("src/main/java/ir/kharjyar/app/weather/WeatherService.kt").readText()
        assertTrue(weather.contains("api.open-meteo.com"))
        assertTrue("دمای هوا باید کش شود", weather.contains("weather_cache"))
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
    fun `card scanning stays on device`() {
        // مدل تشخیص متن داخل خود APK است (نسخه bundled). نسخه وابسته به
        // پلی‌سرویس، مدل را از اینترنت می‌گیرد و وعده آفلاین بودن را می‌شکند.
        val gradle = File("build.gradle.kts").readText()
        assertTrue(
            "تشخیص متن باید با مدل همراه برنامه باشد",
            gradle.contains("mlkit.text.recognition")
        )
        assertFalse(
            "نسخه پلی‌سرویسی مدل را دانلود می‌کند و آفلاین نیست",
            gradle.contains("play-services-mlkit")
        )
        val catalog = File("../gradle/libs.versions.toml").readText()
        assertTrue(catalog.contains("com.google.mlkit"))
        assertFalse(catalog.contains("play-services-mlkit"))
    }

    @Test
    fun `camera permission is only for the card scanner`() {
        assertTrue(
            "اسکن کارت به مجوز دوربین نیاز دارد",
            manifest.contains("android.permission.CAMERA")
        )
        // نبود دوربین نباید نصب برنامه را روی گوشی محدود کند
        assertTrue(manifest.contains("android:required=\"false\""))
        // خواندن گالری یا حافظه لازم نیست؛ فقط فریم زنده دوربین پردازش می‌شود
        assertFalse(manifest.contains("READ_EXTERNAL_STORAGE"))
        assertFalse(manifest.contains("READ_MEDIA_IMAGES"))
    }

    @Test
    fun `card scanner does not touch the network`() {
        listOf(
            "src/main/java/ir/kharjyar/app/ui/components/CardScanner.kt",
            "src/main/java/ir/kharjyar/app/core/card/CardScanParser.kt"
        ).forEach { path ->
            val source = File(path).readText()
            listOf("http://", "https://", "URL(", "OkHttp", "Retrofit").forEach { needle ->
                assertFalse("اسکن کارت نباید به شبکه وصل شود ($needle در $path)", source.contains(needle))
            }
        }
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

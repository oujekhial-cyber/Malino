package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmartCommandCenterDesignGuardTest {
    private val screen=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()

    @Test fun `command center has one clear visual hierarchy`() {
        assertTrue(screen.contains("Brush.linearGradient"))
        assertTrue(screen.contains("بگویید چه کاری برایتان انجام دهم"))
        assertTrue(screen.contains("فرمان شما"))
        assertTrue(screen.contains("modifier=Modifier.fillMaxWidth().height(52.dp)"))
        assertFalse(screen.contains("فرمان‌های قابل انجام"))
    }

    @Test fun `required actions stay available with exact labels`() {
        assertTrue(screen.contains("تشخیص هوش مصنوعی"))
        assertTrue(screen.contains("تشخیص محلی ( آفلاین )"))
        assertTrue(screen.contains("ثبت خودکار"))
        assertTrue(screen.contains("تحلیل پیامک بانکی"))
    }

    @Test fun `examples remain secondary and only appear on an empty draft`() {
        assertTrue(screen.contains("if(input.isBlank()&&parsed==null)"))
        assertTrue(screen.contains("برای شروع، یکی از نمونه‌ها را انتخاب کنید"))
    }
}

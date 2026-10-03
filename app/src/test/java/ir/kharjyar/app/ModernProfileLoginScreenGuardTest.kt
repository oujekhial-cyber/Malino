package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModernProfileLoginScreenGuardTest {
    @Test fun `profile entry follows active app theme and has graphical identity card`() {
        val source=File("src/main/java/ir/kharjyar/app/ui/screens/ProfileScreen.kt").readText()
        listOf("LocalAppSkin.current","HeroCard","SkinCard","settings.cardShine","Brush.radialGradient","CameraAlt","VerifiedUser").forEach { assertTrue(it,source.contains(it)) }
        assertTrue(source.contains("ساخت حساب و ورود"))
        assertTrue(source.contains("اطلاعات شما فقط روی همین دستگاه نگهداری می‌شود"))
        assertTrue(source.contains("username!=profile?.username"))
    }
}

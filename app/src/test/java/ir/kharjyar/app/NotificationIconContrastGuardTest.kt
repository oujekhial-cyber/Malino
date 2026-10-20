package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NotificationIconContrastGuardTest {
    @Test fun `all notifications use the monochrome app icon and explicit accent`() {
        val sources=listOf(
            "src/main/java/ir/kharjyar/app/notify/Notifier.kt",
            "src/main/java/ir/kharjyar/app/work/LifeReminderWorker.kt",
            "src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt"
        ).joinToString("\n"){File(it).readText()}
        assertFalse(sources.contains("setSmallIcon(android.R.drawable"))
        assertTrue(sources.contains("R.drawable.ic_notification_kharjyar"))
        assertTrue(sources.contains("R.color.notification_accent"))
    }

    @Test fun `notification icon is an opaque white alpha mask`() {
        val icon=File("src/main/res/drawable/ic_notification_kharjyar.xml").readText()
        val colors=File("src/main/res/values/colors.xml").readText()
        assertTrue(icon.contains("#FFFFFFFF"))
        assertTrue(colors.contains("notification_accent"))
    }
}

package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetManualAndSixHourRefreshGuardTest {
    @Test fun refreshButtonExistsInEveryLayoutAndReloadsDatabase() {
        listOf("w_split.xml", "w_panels.xml", "w_minimal.xml", "w_royal.xml").forEach { name ->
            val xml = File("src/main/res/layout/$name").readText()
            assertTrue(name, xml.contains("@+id/w_refresh") && xml.contains("@drawable/w_ic_refresh"))
        }
        val widget = File("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt").readText()
        assertTrue(widget.contains("ACTION_REFRESH") && widget.contains("R.id.w_refresh, refreshPending"))
        assertTrue(widget.contains("views.setInt(R.id.w_refresh, \"setImageAlpha\", 190)"))
        assertTrue(widget.contains("6, java.util.concurrent.TimeUnit.HOURS"))
        assertTrue(widget.contains("WidgetRenderer.build(appContext)"))
    }
}

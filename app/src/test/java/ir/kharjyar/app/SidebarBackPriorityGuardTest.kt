package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SidebarBackPriorityGuardTest {
    private val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()

    @Test fun `back closes open sidebar before screen navigation`() {
        val navHost=root.indexOf("NavHost(")
        val priorityHandler=root.indexOf("BackHandler(enabled = drawerState.isOpen)")
        assertTrue(navHost>=0&&priorityHandler>navHost)
        val block=root.substring(priorityHandler,root.indexOf("if (showDirectionChooser)",priorityHandler))
        assertTrue(block.contains("scope.launch { drawerState.close() }"))
        assertTrue(root.contains("صفحه جاری دست‌نخورده بماند"))
    }

    @Test fun `double back exit remains disabled while drawer is open`() {
        assertTrue(root.contains("BackHandler(enabled = currentRoute == \"home\" && !drawerState.isOpen)"))
    }
}

package ir.kharjyar.app

import java.io.File
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ApkSecurityGuardTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()
    private val sources = File("src/main/java").walkTopDown().filter { it.isFile }.joinToString("\n") { it.readText() }

    @Test fun `manifest excludes high risk capabilities`() {
        listOf("REQUEST_INSTALL_PACKAGES", "QUERY_ALL_PACKAGES", "SYSTEM_ALERT_WINDOW", "MANAGE_EXTERNAL_STORAGE", "BIND_ACCESSIBILITY_SERVICE")
            .forEach { assertFalse("High-risk capability found: $it", manifest.contains(it)) }
    }

    @Test fun `app does not download or execute dynamic code`() {
        listOf("DexClassLoader", "PathClassLoader", "Runtime.getRuntime().exec", "ProcessBuilder(")
            .forEach { assertFalse("Dynamic execution primitive found: $it", sources.contains(it)) }
        assertTrue(manifest.contains("android:usesCleartextTraffic=\"false\""))
    }
}

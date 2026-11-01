package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class Android11CompatibilityGuardTest {
    @Test fun `release supports Android 11 while targeting current Android`() {
        val gradle = File("build.gradle.kts").readText()
        assertTrue(gradle.contains("minSdk = 30"))
        assertTrue(gradle.contains("targetSdk = 35"))
    }
}

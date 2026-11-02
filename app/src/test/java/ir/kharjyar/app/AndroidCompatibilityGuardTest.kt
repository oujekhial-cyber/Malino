package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCompatibilityGuardTest {
    @Test fun `release baseline is Android 12 while targeting current Android`() {
        val gradle = File("build.gradle.kts").readText()
        assertTrue(gradle.contains("minSdk = 31"))
        assertTrue(gradle.contains("targetSdk = 35"))
    }
}

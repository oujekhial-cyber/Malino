package ir.kharjyar.app

import ir.kharjyar.app.core.identity.Username
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsernameRulesTest {
    @Test fun `username accepts only lowercase ascii letters digits and underscore`() {
        assertTrue(Username.isValid("ali_1370"))
        assertTrue(Username.isValid("user123"))
        assertFalse(Username.isValid("علی1370"))
        assertFalse(Username.isValid("ab"))
        assertFalse(Username.isValid("ali-name"))
        assertEquals("ali_12", Username.sanitize("علیAli_۱۲12-"))
    }

    @Test fun `sidebar forces username badge to left to right direction`() {
        val root = File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
        assertTrue(root.contains("LocalLayoutDirection provides LayoutDirection.Ltr"))
        assertTrue(root.contains("Text(\"@\${userProfile!!.username}\""))
        val profile = File("src/main/java/ir/kharjyar/app/ui/screens/ProfileScreen.kt").readText()
        assertTrue(profile.contains("KeyboardType.Ascii"))
        assertTrue(profile.contains("TextDirection.Ltr"))
        assertTrue(profile.contains("prefix = { Text(\"@\") }"))
    }
}

package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.parseHomeShortcuts
import org.junit.Assert.*
import org.junit.Test

class RecurringAndHomeFeaturesTest {
    @Test fun `shortcut preferences preserve order and reject unknown destinations`() {
        assertEquals(listOf("globalSearch", "manual"), parseHomeShortcuts("globalSearch,invalid,manual,globalSearch"))
        assertTrue(parseHomeShortcuts("").isEmpty())
    }
}

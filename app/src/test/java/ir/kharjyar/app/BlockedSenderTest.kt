package ir.kharjyar.app

import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.BlockedSenderEntity
import ir.kharjyar.app.data.prefs.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** موجودیت فرستنده مسدود و پیش‌فرض واحد پول. */
class BlockedSenderTest {

    @Test
    fun `blocked sender keeps sender and timestamp`() {
        val now = 1_700_000_000_000L
        val e = BlockedSenderEntity(sender = "Digikala", createdAt = now)
        assertEquals("Digikala", e.sender)
        assertEquals(now, e.createdAt)
        // شناسه پیش‌فرض صفر است تا Room خودش تولیدش کند
        assertEquals(0L, e.id)
    }

    @Test
    fun `senders differing in case are distinct entities`() {
        val a = BlockedSenderEntity(sender = "SAMAN", createdAt = 1L)
        val b = BlockedSenderEntity(sender = "saman", createdAt = 1L)
        assertNotEquals(a.sender, b.sender)
    }

    @Test
    fun `default money unit is rial`() {
        assertEquals(MoneyUnit.RIAL, AppSettings().moneyUnit)
    }
}

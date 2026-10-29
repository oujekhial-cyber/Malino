package ir.kharjyar.app

import ir.kharjyar.app.ui.components.formatCardExpiry
import org.junit.Assert.assertTrue
import org.junit.Test

class CardExpiryVisualOrderTest {
    @Test fun `expiry displays year on physical left and month on physical right`() {
        val shown = formatCardExpiry("09/1405")
        assertTrue(shown.contains("۱۴۰۵/۰۹"))
        assertTrue(shown.startsWith("\u2066") && shown.endsWith("\u2069"))
    }

    @Test fun `persian input and alternate separator keep reversed visual order`() {
        assertTrue(formatCardExpiry("۰۶-۱۴۰۸").contains("۱۴۰۸/۰۶"))
    }
}

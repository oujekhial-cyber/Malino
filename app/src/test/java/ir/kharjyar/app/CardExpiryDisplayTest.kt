package ir.kharjyar.app

import ir.kharjyar.app.core.card.CardExpiry
import org.junit.Assert.assertEquals
import org.junit.Test

class CardExpiryDisplayTest {
    @Test fun `month is displayed on right and year on left`() {
        assertEquals("1408/06", CardExpiry.storageToDisplay("06/1408"))
        assertEquals("28/06", CardExpiry.storageToDisplay("06/28"))
    }

    @Test fun `display value returns to compatible month year storage`() {
        assertEquals("06/1408", CardExpiry.displayToStorage("1408/06"))
        assertEquals("1408/06", CardExpiry.formatDisplayInput("۱۴۰۸۰۶"))
    }
}

package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtPersonCardDateGuardTest {
    @Test fun `person card spotlights nearest open receivable and payable dates`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        listOf(
            "receivableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()",
            "payableDebts.filter { remaining(it) > 0 }.mapNotNull { it.dueAt }.minOrNull()",
            "label = \"موعد دریافت طلب\"",
            "label = \"موعد پرداخت بدهی\"",
            "private fun DebtDueSpotlight(",
            "style = MaterialTheme.typography.titleLarge",
            "روز از موعد گذشته",
            "days == 0 -> \"امروز\"",
            "days == 1 -> \"فردا\"",
            "CardDefaults.cardElevation(defaultElevation = 4.dp)"
        ).forEach { assertTrue(it, source.contains(it)) }
    }
}

package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtPersonCardDateGuardTest {
    @Test fun `person card shows nearest open receivable and payable dates`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        listOf(
            "receivableDebts.filter { remaining(it) > 0 }.minOfOrNull { it.dueAt }",
            "payableDebts.filter { remaining(it) > 0 }.minOfOrNull { it.dueAt }",
            "label = \"تاریخ دریافت طلب\"",
            "label = \"تاریخ پرداخت بدهی\"",
            "PersianDate.fromMillis(it).format()",
            "private fun DebtDateRow("
        ).forEach { assertTrue(it, source.contains(it)) }
    }
}

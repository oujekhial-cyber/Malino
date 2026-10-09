package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtPersonCardDesignGuardTest {
    @Test fun `person debt cards show relationship and themed financial graphics`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        listOf(
            "شما از ${'$'}{person.name} طلبکار هستید",
            "شما به ${'$'}{person.name} بدهکار هستید",
            "DebtAmountTile(",
            "Brush.linearGradient",
            "relationAccent.copy(alpha = .16f)",
            "Icons.Filled.CallReceived",
            "Icons.Filled.CallMade",
            "title = \"طلب شما\"",
            "title = \"بدهی شما\"",
            "مشاهده گردش حساب"
        ).forEach { assertTrue(it, source.contains(it)) }
    }
}

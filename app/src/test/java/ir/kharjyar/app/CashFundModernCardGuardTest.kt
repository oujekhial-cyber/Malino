package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CashFundModernCardGuardTest {
    @Test fun `cash accounts use a dedicated modern graphical card everywhere`() {
        val card=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
        listOf(
            "accountType == AccountType.CASH",
            "CashFundCard(",
            "Brush.linearGradient",
            "Brush.radialGradient",
            "Icons.Filled.Payments",
            "CashMetaPill(Icons.Filled.Person",
            "CashMetaPill(Icons.Filled.Place",
            "صندوق نقدی",
            "Color(0xFFFFD978)"
        ).forEach { assertTrue(it,card.contains(it)) }
        val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        listOf("accountType = account.accountType","ownerName = account.ownerName","cashLocation = account.cashLocation").forEach { assertTrue(it,dashboard.contains(it)) }
        val editor=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
        assertTrue(editor.contains("accountType = accountType"))
        assertTrue(editor.contains("ownerName = ownerName"))
    }
}

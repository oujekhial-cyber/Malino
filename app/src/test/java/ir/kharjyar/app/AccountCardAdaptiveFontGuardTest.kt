package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountCardAdaptiveFontGuardTest {
 @Test fun `account card height grows with effective user font scale`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(source.contains("val accountFontScale=LocalDensity.current.fontScale"))
  assertTrue(source.contains("(accountFontScale-1f).coerceAtLeast(0f)*190f"))
  assertTrue(source.contains("val accountCarouselHeight = compactAccountCardHeight + 10.dp"))
 }

 @Test fun `balances use separate readable visual tiles instead of one tiny line`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(source.contains("private fun AccountBalanceTile"))
  assertTrue(source.contains("AccountBalanceWallet"))
  assertTrue(source.contains("Icons.Filled.Sms"))
  assertTrue(source.contains("style=MaterialTheme.typography.bodyMedium"))
  assertTrue(source.contains("maxLines=2"))
 }

 @Test fun `deposit and withdrawal remain vertically adaptive and visible`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(source.contains("CompactAccountFlow(\"واریز\""))
  assertTrue(source.contains("CompactAccountFlow(\"برداشت\""))
  assertTrue(source.contains("private fun CompactAccountFlow"))
  assertTrue(source.contains("verticalArrangement=Arrangement.spacedBy(3.dp)"))
 }
}

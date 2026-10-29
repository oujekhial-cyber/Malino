package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalInteractiveReportGuardTest {
 @Test fun `expense is default and income expense tiles switch pie details`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
  assertTrue(s.contains("mutableStateOf(TxNature.EXPENSE)"))
  assertTrue(s.contains("onClick={selectedBreakdown=TxNature.INCOME}"))
  assertTrue(s.contains("onClick={selectedBreakdown=TxNature.EXPENSE}"))
  assertTrue(s.contains("incomeBreakdown"))
  assertTrue(s.contains("expenseBreakdown"))
  assertTrue(s.contains("درآمدها از چه محل‌هایی بوده؟"))
  assertTrue(s.contains("مخارج صرف چه چیزهایی شده؟"))
  assertTrue(s.contains("میانگین هر تراکنش"))
  assertTrue(s.contains("بیشترین محل هزینه"))
  assertTrue(s.contains("selectedTransactions.take(20)"))
 }
 @Test fun `transaction account combo starts with all accounts`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(s.contains("var filterAccount by remember { mutableStateOf<Long?>(null) }"))
  assertTrue(s.contains("options=listOf<Long?>(null)+accounts.filterNot{it.archived}.map{it.id}"))
  assertTrue(s.contains("if(id==null)\"همه حساب‌ها\""))
 }
}

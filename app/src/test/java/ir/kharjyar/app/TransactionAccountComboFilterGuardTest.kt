package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TransactionAccountComboFilterGuardTest {
 @Test fun `account filter is a classic combo beside month filtering`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  val month=source.indexOf("ComboBox(\"ماه\"")
  val account=source.indexOf("label=\"حساب\"",month)
  assertTrue(month>=0)
  assertTrue(account>month)
  assertTrue(source.contains("options=listOf<Long?>(null)+accounts.filterNot{it.archived}.map{it.id}"))
  assertTrue(source.contains("onSelect={filterAccount=it}"))
  assertTrue(source.contains("if(id==null)\"همه حساب‌ها\""))
 }
}

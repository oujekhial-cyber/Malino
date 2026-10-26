package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionCollapsingFiltersGuardTest {
 @Test fun `filters and summary share exact transaction scroll then summary sticks`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(source.contains("val transactionListState = rememberLazyListState()"))
  assertTrue(source.contains("item(key = \"transaction-filters\")"))
  assertTrue(source.contains("stickyHeader(key = \"transaction-summary\")"))
  assertTrue(source.contains("state = transactionListState"))
  val list=source.indexOf("LazyColumn(\n            state = transactionListState")
  val filters=source.indexOf("item(key = \"transaction-filters\")",list)
  val summary=source.indexOf("stickyHeader(key = \"transaction-summary\")",filters)
  val rows=source.indexOf("itemsIndexed(",summary)
  assertTrue(list>=0)
  assertTrue(filters>list)
  assertTrue(summary>filters)
  assertTrue(rows>summary)
  assertFalse(source.contains("visible = showInlineFilters"))
  assertFalse(source.contains("animationSpec = tween(520)"))
 }
}

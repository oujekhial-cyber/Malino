package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionCollapsingFiltersGuardTest {
 @Test fun `filters collapse with list scroll while financial summary stays outside list`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(source.contains("val transactionListState = rememberLazyListState()"))
  assertTrue(source.contains("transactionListState.firstVisibleItemIndex == 0"))
  assertTrue(source.contains("transactionListState.firstVisibleItemScrollOffset < 12"))
  assertTrue(source.contains("AnimatedVisibility("))
  assertTrue(source.contains("visible = showInlineFilters"))
  assertTrue(source.contains("expandVertically("))
  assertTrue(source.contains("shrinkVertically("))
  assertTrue(source.contains("animationSpec = tween(520)"))
  assertTrue(source.contains("state = transactionListState"))
  val filters=source.indexOf("visible = showInlineFilters")
  val summary=source.indexOf("Text(\"درآمد واقعی\"",filters)
  val list=source.indexOf("LazyColumn(",summary)
  assertTrue(filters>=0)
  assertTrue(summary>filters)
  assertTrue(list>summary)
 }
}

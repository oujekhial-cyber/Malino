package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFilterSessionGuardTest {
 @Test fun `transactions default to preferred account and current month and retain session navigation`() {
  val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(vm.contains("data class TransactionFilterSession"))
  assertTrue(vm.contains("MutableStateFlow<TransactionFilterSession?>(null)"))
  assertFalse(vm.contains("transactionFilterSession") && vm.contains("SavedStateHandle"))
  assertTrue(screen.contains("settings.defaultAccountId?.takeIf"))
  assertTrue(screen.contains("currentTransactionMonth.year"))
  assertTrue(screen.contains("currentTransactionMonth.month"))
  assertTrue(screen.contains("viewModel.transactionFilterSession.collectAsState()"))
  assertTrue(screen.contains("viewModel.setTransactionAccountFilter(it)"))
  assertTrue(screen.contains("viewModel.setTransactionMonthFilter(year.takeIf { it != 0 }, selectedFilterMonth)"))
  assertTrue(screen.contains("viewModel.setTransactionMonthFilter(selectedFilterYear, month.takeIf { it != 0 })"))
 }
}

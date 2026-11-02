package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class TransactionEasyFiltersGuardTest {
 @Test fun `transaction filters expose one tap shortcuts and simple sheet`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText();listOf("فیلترهای پرکاربرد","ProfessionalFilterChip(\"همه\"","ProfessionalFilterChip(\"واریز\"","ProfessionalFilterChip(\"برداشت\"","ModalBottomSheet","نوع گردش","ماهیت تراکنش","همه حساب‌ها","پاک کردن","نتیجه").forEach{assertTrue(it, s.contains(it))};assertFalse(s.substringAfter("if (showFilters)").substringBefore("if (pendingDelete").contains("ComboBox"))}
}

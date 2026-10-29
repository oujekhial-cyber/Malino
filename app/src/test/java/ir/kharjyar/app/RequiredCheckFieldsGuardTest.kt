package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class RequiredCheckFieldsGuardTest {
 @Test fun `sayad amount and due date are required and obsolete fields are removed`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  listOf("مبلغ چک به عدد *","unit=settings.moneyUnit","شناسه ۱۶ رقمی صیادی *","تاریخ سررسید *","sayad.length==16").forEach{assertTrue(it,source.contains(it))}
  assertTrue(source.contains("label={Text(\"طرف حساب اصلی *\")}"))
  assertFalse(source.contains("مبلغ چک به حروف"))
 }
 @Test fun `issuer is requested only for received checks`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("if(received)OutlinedTextField(issuer"))
  assertTrue(source.contains("else OutlinedTextField(receiver"))
  assertTrue(source.contains("p.issuerName.isNotBlank()&&received"))
 }
}

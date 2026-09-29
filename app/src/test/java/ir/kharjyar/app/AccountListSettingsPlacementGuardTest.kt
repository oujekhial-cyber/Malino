package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class AccountListSettingsPlacementGuardTest {
 @Test fun `account list navigation is under default account selector`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val page=source.substring(source.indexOf("if (section == \"account\")"),source.indexOf("// ---------- پول ----------"))
  val selector=page.indexOf("حساب پیش‌فرض داشبورد و ویجت")
  val accountList=page.indexOf("NavRow(\"لیست حساب‌ها\")")
  assertTrue(selector>=0)
  assertTrue(accountList>selector)
  assertTrue(page.substring(accountList).contains("nav.navigate(\"accounts\")"))
 }
}

package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class SidebarAccountLabelGuardTest {
 @Test fun `sidebar uses account management wording`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  assertTrue(source.contains("DrawerEntry(\"accounts\", \"مدیریت حساب‌ها\""))
 }
}

package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class CivicCenterCategoryNavigationGuardTest {
 @Test fun `citizen center presents clickable sections and dedicated lists`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/CivicCenterScreen.kt").readText()
  listOf("قبوض شهروندی","جرائم راهنمایی و رانندگی","قبوض خدماتی","بیمه و خدمات پوشش","ابلاغیه‌ها و عدل‌ایران","clickable{selectedKind=kind}","messages.filter{it.kind==kind}","بازگشت به قبوض شهروندی").forEach{assertTrue(it,source.contains(it))}
  assertFalse(source.contains("Text(\"قبوض و جرائم مالی\",style=MaterialTheme.typography.headlineSmall"))
 }
 @Test fun `sidebar uses new citizen center name`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  assertTrue(source.contains("DrawerEntry(\"civicCenter\", \"قبوض شهروندی\""))
 }
}

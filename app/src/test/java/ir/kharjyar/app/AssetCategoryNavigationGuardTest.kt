package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class AssetCategoryNavigationGuardTest {
 @Test fun `assets open from category landing into dedicated type lists`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  listOf("AssetCategoryLanding","selectedKind==null","allActive.filter{it.kind==selectedKind}","دارایی‌های طلا","خودروها","املاک، خانه و زمین","سایر دارایی‌ها","clickable{onSelect(kind)}","بازگشت به انواع دارایی").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `new asset opened inside category starts with that type`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  assertTrue(source.contains("AssetEditor(vm,editor,selectedKind?:AssetKind.GOLD"))
  assertTrue(source.contains("existing?.kind?:initialKind"))
 }
}

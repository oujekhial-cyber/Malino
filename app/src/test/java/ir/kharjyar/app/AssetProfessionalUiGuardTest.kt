package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class AssetProfessionalUiGuardTest {
 @Test fun `assets have graphical portfolio and swipe edit delete`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("نمای کلی سبد دارایی شما"));assertTrue(s.contains("ModernSummaryHero"));assertTrue(s.contains("SwipeActionRow"));assertTrue(s.contains("onDelete={pendingDelete=asset}"));assertTrue(s.contains("onEdit={editor=asset}"));assertTrue(s.contains("ویرایش دارایی")&&s.contains("حذف دارایی"))}
 @Test fun `asset deletion also removes trades`(){val dao=File("src/main/java/ir/kharjyar/app/data/db/AssetEntities.kt").readText();assertTrue(dao.contains("deleteTrades")&&dao.contains("DELETE FROM assets"))}
}

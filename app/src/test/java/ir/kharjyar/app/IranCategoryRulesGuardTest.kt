package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class IranCategoryRulesGuardTest {
 @Test fun `common Iranian automatic rule samples are seeded once and editable`(){val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText();assertTrue(db.contains("DEFAULT_IRAN_RULES"));listOf("اسنپ","تپسی","افق کوروش","دیجی‌کالا","داروخانه","همراه اول","قبض برق","بیمه ایران").forEach{assertTrue(db.contains("\"$it\""))};assertTrue(db.contains("NOT EXISTS (SELECT 1 FROM category_rules"));assertTrue(db.contains("createdByUser, createdAt"));val ui=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText();assertTrue(ui.contains("نمونه پیشنهادی متداول در ایران"))}
}

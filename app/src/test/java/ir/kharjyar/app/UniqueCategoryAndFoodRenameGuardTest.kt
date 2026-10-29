package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniqueCategoryAndFoodRenameGuardTest {
    @Test fun `category names are unique in database and guarded in UI`() {
        val entities=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
        val daos=File("src/main/java/ir/kharjyar/app/data/db/Daos.kt").readText()
        val screen=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
        assertTrue(entities.contains("Index(value = [\"name\"], unique = true)"))
        assertTrue(daos.contains("@Insert(onConflict = OnConflictStrategy.IGNORE)"))
        assertTrue(screen.contains("normalizedCategoryName"))
        assertTrue(screen.contains("دسته‌ای با این نام از قبل وجود دارد"))
    }

    @Test fun `food category is renamed everywhere and migration preserves links`() {
        val production=File("src/main/java").walkTopDown().filter{it.isFile&&it.extension=="kt"}.joinToString("\n"){it.readText()}
        assertTrue(production.contains("مواد غذایی و سوپرمارکت"))
        // نام قدیمی فقط باید داخل SQL مهاجرت برای تبدیل داده کاربران قبلی باقی بماند.
        val outsideMigration=production.replace(Regex("db\\.execSQL\\([^\\n]+خوراک و سوپرمارکت[^\\n]+"),"")
        assertFalse(outsideMigration.contains("خوراک و سوپرمارکت"))
        val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
        assertTrue(db.contains("UPDATE transactions SET categoryId"))
        assertTrue(db.contains("UPDATE category_rules SET categoryId"))
        assertTrue(db.contains("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name"))
    }
}

package ir.kharjyar.app

import ir.kharjyar.app.ui.components.categoryIconFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingCart
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryGraphicIconsGuardTest {
    @Test fun `common Persian categories receive related icons`() {
        assertEquals(Icons.Filled.ShoppingCart, categoryIconFor("مواد غذایی و سوپرمارکت"))
        assertEquals(Icons.Filled.DirectionsCar, categoryIconFor("حمل و نقل و خودرو"))
        assertEquals(Icons.Filled.LocalHospital, categoryIconFor("درمان و سلامت"))
    }

    @Test fun `category list and editor use graphical category component`() {
        val source=File("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt").readText()
        assertTrue(source.contains("CategoryGraphic(name = c.name"))
        assertTrue(source.contains("CategoryGraphic(name = name"))
        val graphic=File("src/main/java/ir/kharjyar/app/ui/components/CategoryGraphic.kt").readText()
        listOf("Brush.linearGradient","contentDescription = \"آیکون \$name\"","Icons.Filled.Category").forEach { assertTrue(it,graphic.contains(it)) }
    }
}

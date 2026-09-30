package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketPopupReadabilityGuardTest {
    @Test
    fun `market popup has compact overlaid header and light and dark palettes`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        listOf(
            "fillMaxWidth(.90f)",
            "padding(top=18.dp",
            "val darkMarket=MaterialTheme.colorScheme.background.luminance()<.5f",
            "if(darkMarket)Color(0xFF0B101B) else Color(0xFFF9FAFF)",
            "Modifier.height(45.dp).fillMaxWidth()",
            "Color(0xFF55D9D5).copy(alpha=.18f)",
            "MaterialTheme.typography.titleLarge",
            "MaterialTheme.typography.bodySmall",
            "PremiumMarketTile",
            "darkMarket",
            "Color(0xFFFFFFFF)",
            "\"Au\"",
            "\"\\$\"",
            "آخرین تغییرات بازار",
            "آخرین بروزرسانی"
        ).forEach { assertTrue(it, source.contains(it)) }
    }
}

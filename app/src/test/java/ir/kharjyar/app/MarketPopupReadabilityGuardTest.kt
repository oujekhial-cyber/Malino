package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketPopupReadabilityGuardTest {
    @Test fun `market popup is anchored compact glassy and has solid rate cards`() {
        val source=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        listOf(
            "popupPositionProvider=object:PopupPositionProvider",
            "anchorBounds.bottom+marketBarGapPx",
            "fillMaxWidth(.86f)",
            "panelBase.copy(alpha=if(darkMarket).86f else .84f)",
            "Modifier.size(38.dp)",
            "MaterialTheme.typography.titleMedium",
            "MaterialTheme.typography.labelSmall",
            "verticalArrangement=Arrangement.spacedBy(0.dp)",
            "color=tile.copy(alpha=if(dark).98f else 1f)",
            "PremiumMarketTile","\"Au\"","\"\\$\"","آخرین تغییرات بازار","آخرین بروزرسانی"
        ).forEach{assertTrue(it,source.contains(it))}
        val popup=source.substring(source.indexOf("if(expanded)Popup"),source.indexOf("@Composable private fun PremiumMarketTile"))
        assertFalse(popup.contains("Icons.Filled.Close"))
        assertFalse(popup.contains("contentDescription=\"بستن\""))
    }
}

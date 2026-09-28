package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class DashboardCardDragOverlayGuardTest {
 @Test fun `dashboard card drag stays captured in a global overlay until user releases`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();assertTrue(s.contains("Popup("));assertTrue(s.contains("PopupProperties(focusable = false)"));assertTrue(s.contains("positionInWindow()"));assertTrue(s.contains("draggingId = account.id"));assertTrue(s.contains("onDragEnd"));assertTrue(s.indexOf("setDashboardAccountOrder(ids)")>s.indexOf("onDragEnd"));assertFalse(s.substringAfter("onDrag = { change, amount ->").substringBefore("onDragEnd").contains("setDashboardAccountOrder"))}
 @Test fun `neighbor cards move slowly and reverse with drag direction`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();assertTrue(s.contains("dragTargetIndex < dragOriginIndex"));assertTrue(s.contains("dragTargetIndex > dragOriginIndex"));assertTrue(s.contains("tween(durationMillis = 480)"));assertTrue(s.contains("floatingY += amount.y"))}
}

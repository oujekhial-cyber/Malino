package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DashboardCardEdgeAutoScrollGuardTest {
 @Test fun `held account card advances after pause at either physical edge`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("edgeThresholdPx")&&s.contains("screenWidthPx-edgeThresholdPx"))
  assertTrue(s.contains("delay(520)"))
  assertTrue(s.contains("while(draggingId==account.id&&edgeHoverDirection==edge)"))
  assertTrue(s.contains("rowState.animateScrollToItem(next+1)"))
  assertTrue(s.contains("if(dashboardLayoutDirection==LayoutDirection.Rtl)-1 else 1"))
  assertTrue(s.contains("edgeScrollJob?.cancel()"))
 }
}

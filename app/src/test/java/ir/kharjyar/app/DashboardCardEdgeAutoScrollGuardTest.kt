package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DashboardCardEdgeAutoScrollGuardTest {
 @Test fun `held account card advances one animated slot per pause and can reverse`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("edgeThresholdPx")&&s.contains("screenWidthPx-edgeThresholdPx"))
  assertTrue(s.contains("delay(680)"))
  assertTrue(s.contains("while(draggingId==account.id&&dragTargetIndex!=hoverTargetIndex)"))
  assertTrue(s.contains("val step=if(hoverTargetIndex>dragTargetIndex)1 else -1"))
  assertTrue(s.contains("dragTargetIndex=(dragTargetIndex+step).coerceIn(active.indices)"))
  assertTrue(s.contains("val settledIndex=dragTargetIndex"))
  assertTrue(s.contains("rowState.animateScrollToItem(settledIndex+1)"))
  assertTrue(s.indexOf("val settledIndex=dragTargetIndex") < s.indexOf("rowState.animateScrollToItem(settledIndex+1)"))
  assertTrue(s.contains("reorderStepJob?.cancel()"))
  assertTrue(s.contains("durationMillis = 560")&&s.contains("neighborCardSlowShift"))
 }
}

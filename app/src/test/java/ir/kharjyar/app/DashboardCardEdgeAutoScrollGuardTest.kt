package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DashboardCardEdgeAutoScrollGuardTest {
 @Test fun `account reorder remains held and follows cumulative distance in either direction`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(s.contains("floatingX/(physicalStep*indexDirection)"))
  assertTrue(s.contains("dragTargetIndex=(dragOriginIndex+kotlin.math.round"))
  assertTrue(s.contains("translationX = if (isDragging) floatingX else neighborOffset"))
  assertTrue(s.contains("draggingId = null; floatingX = 0f; floatingY = 0f;dragOriginIndex=-1;dragTargetIndex=-1"))
  assertFalse(s.contains("reorderStepJob"))
  assertFalse(s.contains("hoverTargetIndex"))
 }
}

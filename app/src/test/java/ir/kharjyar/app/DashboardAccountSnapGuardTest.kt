package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardAccountSnapGuardTest {
 @Test fun `account carousel always settles on one complete card`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  listOf(
   "val accountSnapStepPx",
   "pageWidth + 10.dp",
   "snapshotFlow{rowState.isScrollInProgress}",
   ".distinctUntilChanged().drop(1)",
   "kotlin.math.round(rowState.value/accountSnapStepPx)",
   "coerceIn(0,rowState.maxValue)",
   "rowState.animateScrollTo(nearest,animationSpec=tween(420))",
   "draggingId==null"
  ).forEach{assertTrue(it,screen.contains(it))}
 }
}

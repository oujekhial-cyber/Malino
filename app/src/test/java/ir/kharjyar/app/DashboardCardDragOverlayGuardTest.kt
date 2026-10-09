package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardCardDragOverlayGuardTest{
 @Test fun `account cards use same in-place multi-card movement as vehicle plates`(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  listOf("previewIds","ids.removeAt(dragOriginIndex)","ids.add(dragTargetIndex, draggingId!!)","liveShiftPx","floatingX/(physicalStep*indexDirection)","kotlin.math.round","coerceIn(active.indices)","translationX = if (isDragging) floatingX else neighborOffset","tween(durationMillis = 620)","animateItemPlacement(animationSpec = tween(durationMillis = 760))").forEach{assertTrue(it,s.contains(it))}
  assertFalse("dragged card must stay in place instead of a disposable popup",s.contains("floatingOriginX + floatingX).roundToInt()"))
  assertTrue(s.contains("floatingY += amount.y"))
 }
}

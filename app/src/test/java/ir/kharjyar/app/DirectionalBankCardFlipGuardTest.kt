package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DirectionalBankCardFlipGuardTest {
 @Test fun `account card repeats vertical swipe direction and ignores horizontal swipe`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains("detectVerticalDragGestures"))
  assertTrue(s.contains("verticalDrag+=amount"))
  assertTrue(s.contains("val gestureDirection=if(verticalDrag>0f)-1f else 1f"))
  assertTrue(s.contains("val visibleFaceDirection=if(turns%2==0)1f else -1f"))
  assertTrue(s.contains("targetRotation+=gestureDirection*visibleFaceDirection*180f"))
  assertTrue(s.contains("rotationX = flipRotation")&&s.contains("rotationX = 180f"))
  assertFalse(s.contains("rotationY = flipRotation"))
  assertFalse(s.contains("horizontalFlip"))
 }
 @Test fun `flip uses inner glass sheen instead of ugly outer shadow`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains("shadowElevation = 0f"))
  assertTrue(s.contains("onCard.copy(alpha=.16f*flipWave)"))
  assertTrue(s.contains("base.lighten(.28f).copy(alpha=.10f*flipWave)"))
 }
}

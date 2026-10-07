package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DirectionalBankCardFlipGuardTest {
 @Test fun `account card follows horizontal and vertical swipe direction`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains("detectDragGestures"))
  assertTrue(s.contains("dragX+=amount.x")&&s.contains("dragY+=amount.y"))
  assertTrue(s.contains("horizontalFlip=horizontal"))
  assertTrue(s.contains("targetRotation+=direction*180f"))
  assertTrue(s.contains("if(horizontalFlip) rotationY = flipRotation else rotationX = flipRotation"))
  assertTrue(s.contains("if(horizontalFlip) rotationY = 180f else rotationX = 180f"))
 }
}

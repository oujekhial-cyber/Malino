package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CardFlipAndMarketInteractionGuardTest {
 @Test fun bankCardFlipsVerticallyAndDetailsLiveOnBack(){
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains("detectVerticalDragGestures"))
  assertTrue(s.contains("rotationX = flipRotation")&&s.contains("durationMillis = 920"))
  assertTrue(s.contains("bankCardCinematicFlip")&&s.contains("CubicBezierEasing"))
  assertTrue(s.contains("scaleX = 1f - (.035f * flipWave)")&&s.contains("shadowElevation = 18.dp.toPx() * flipWave"))
  assertTrue(s.contains("اطلاعات کامل حساب")&&s.contains("شماره حساب")&&s.contains("شبا")&&s.contains("CVV2"))
  assertTrue(!s.contains("AnimatedVisibility(\n            visible = selected && showDetailsWhenSelected"))
 }
 @Test fun marketHasThreeGroupsPullAndPerRowSwipeRefresh(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/MarketPulseScreen.kt").readText()
  assertTrue(s.contains("\"طلا و نقره\"")&&s.contains("\"سکه\"")&&s.contains("\"ارز\""))
  assertTrue(s.contains("detectVerticalDragGestures")&&s.contains("detectHorizontalDragGestures"))
  assertTrue(s.contains("Text(\"بروزرسانی\""))
  assertTrue(s.contains("contentAlignment=if(dragX>=0)Alignment.CenterStart else Alignment.CenterEnd"))
 }
}

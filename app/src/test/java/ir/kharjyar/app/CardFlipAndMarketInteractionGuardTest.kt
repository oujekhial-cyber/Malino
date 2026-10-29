package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CardFlipAndMarketInteractionGuardTest {
 @Test fun bankCardFollowsRepeatedVerticalSwipeDirectionAndDetailsLiveOnBack(){
  val s=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(s.contains("detectVerticalDragGestures"))
  assertTrue(s.contains("rotationX = flipRotation")&&s.contains("durationMillis = 880"))
  assertTrue(s.contains("bankCardDirectionalFlip")&&s.contains("CubicBezierEasing(.22f, 0f, .18f, 1f)"))
  assertTrue(s.contains("scaleX = 1f - (.025f * flipWave)")&&s.contains("shadowElevation = 0f"))
  assertTrue(!s.contains("border(borderWidth, borderColor, shape)")&&!s.contains("لبه کارت برای یک لحظه"))
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

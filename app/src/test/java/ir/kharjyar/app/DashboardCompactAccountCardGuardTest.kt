package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class DashboardCompactAccountCardGuardTest {
 @Test fun accountCardIsUnifiedCompactAndExpandsOnlyWhenSelected(){
  val d=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();val card=File("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt").readText()
  assertTrue(d.contains(".height(if(defaultAccount?.id == account.id) 365.dp else 205.dp)"))
  assertTrue(d.contains("showDetailsWhenSelected = true")&&d.contains("AnimatedVisibility(defaultAccount?.id == account.id)"))
  assertFalse("A detached second account card must not return",d.contains("Spacer(Modifier.height(8.dp))\n                                AccountBelowCardPanel"))
  assertTrue(card.contains("visible = selected && showDetailsWhenSelected")&&card.contains("content()"))
 }
}

package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class SimpleTransactionEntryGuardTest{
 @Test fun dailyFormIsShortAndAdvancedFieldsAreProgressive(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
  assertTrue(s.contains("SimpleDirectionSelector")&&s.contains("اطلاعات اصلی"))
  assertTrue(s.contains("جزئیات بیشتر")&&s.contains("AnimatedVisibility(detailsOpen)"))
  assertTrue(s.indexOf("AmountTextField")<s.indexOf("CategoryPicker"))
  assertTrue(s.contains("defaultAccount?.id")&&s.contains("ثبت سریع")&&s.contains("از پیامک"))
  assertFalse("The confusing dual direction/nature picker must not return",s.contains("NaturePicker("))
 }
}

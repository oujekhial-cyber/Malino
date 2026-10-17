package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class SimpleTransactionEntryGuardTest{
 @Test fun dailyFormIsShortAndAdvancedFieldsAreProgressive(){
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
  assertTrue(s.contains("NaturePicker(nature,direction")&&s.contains("اطلاعات اصلی"))
  assertTrue(s.contains("جزئیات بیشتر")&&s.contains("AnimatedVisibility(detailsOpen)"))
  assertTrue(s.indexOf("AmountTextField")<s.indexOf("CategoryPicker"))
  assertTrue(s.contains("defaultAccount?.id")&&s.contains("ثبت سریع")&&s.contains("از پیامک"))
  val picker=File("src/main/java/ir/kharjyar/app/ui/screens/TxForm.kt").readText()
  assertTrue("One unified flow picker must replace the confusing dual fields",picker.contains("label=\"نوع جریان مالی\"")&&!picker.contains("label = \"جهت بانکی\""))
 }
}

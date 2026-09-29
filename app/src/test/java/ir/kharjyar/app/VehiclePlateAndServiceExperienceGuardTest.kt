package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class VehiclePlateAndServiceExperienceGuardTest {
 @Test fun `natural plate display default auto advance letter wheel and swipe actions exist`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("12س345ایران11","car.leftTwo","car.letter","car.serialThree","car.iranCode","showLetters=true","حرف پلاک را بچرخانید","rememberSnapFlingBehavior","serialFocus.requestFocus()","iranFocus.requestFocus()","SwipeActionRow(onDelete={pendingDelete=vehicle}").forEach{assertTrue(it,s.contains(it))}
 }
 @Test fun `service picker oil parts and timing belt mileage are available`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val e=File("src/main/java/ir/kharjyar/app/data/db/CivicEntities.kt").readText()
  listOf("ComboBox(\"انتخاب وسیله نقلیه\"","تعویض تسمه تایم","60000","فیلتر روغن","فیلتر اتاق","فیلتر هوا","صافی بنزین","روغن هیدرولیک","روغن ترمز","روغن گیربکس","بررسی شد","تعویض شد","هرکدام که زودتر فرا برسد").forEach{assertTrue(it,s.contains(it))}
  assertTrue(e.contains("serviceType:String"));assertTrue(e.contains("partsStatus:String"))
 }
 @Test fun `split widget remains default and title is top right`() {
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText();val xml=File("src/main/res/layout/w_split.xml").readText()
  assertTrue(prefs.contains("widgetLayout: WidgetLayout = WidgetLayout.SPLIT"));assertTrue(xml.contains("android:id=\"@+id/w_app_area\"")&&xml.contains("android:gravity=\"top|end\"")&&xml.contains("android:id=\"@+id/w_title\""))
 }
}

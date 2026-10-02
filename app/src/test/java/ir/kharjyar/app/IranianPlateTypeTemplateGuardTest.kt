package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IranianPlateTypeTemplateGuardTest {
 @Test fun commonIranianPlateTypesColorsAndLettersAreAvailable(){
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("TAXI(\"تاکسی\"","PUBLIC(\"عمومی/باربری\"","TEMPORARY(\"گذر موقت\"","FREE_ZONE(\"منطقه آزاد\"","AGRICULTURAL(\"کشاورزی\"").forEach{assertTrue(source.contains(it))}
  assertTrue(source.contains("Color(0xFFFFC928)"))
  assertTrue(source.contains("\"ت\",Color")&&source.contains("\"ع\",Color")&&source.contains("\"گ\",Color"))
 }
 @Test fun freeZonesAndVehicleDrivenDefaultsAreCovered(){
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("کیش","قشم","اروند","انزلی","ارس","ماکو","چابهار").forEach{assertTrue(source.contains(it))}
  assertTrue(source.contains("\"کامیون\",\"تریلر\",\"اتوبوس\",\"مینی‌بوس\"->PlateKind.PUBLIC"))
  assertTrue(source.contains("\"تاکسی\"->PlateKind.TAXI"))
 }
}

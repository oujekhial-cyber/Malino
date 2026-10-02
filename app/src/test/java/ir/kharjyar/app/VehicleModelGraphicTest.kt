package ir.kharjyar.app
import ir.kharjyar.app.ui.components.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class VehicleModelGraphicTest{
 @Test fun knownModelsAndTypesResolveToDifferentArtwork(){
  assertEquals(VehicleGraphicKind.PEUGEOT_PARS,vehicleGraphicKind("پژو پارس سال","خودرو سواری"))
  assertEquals(VehicleGraphicKind.PEUGEOT_206,vehicleGraphicKind("پژو ۲۰۶","خودرو سواری"))
  assertEquals(VehicleGraphicKind.SHAHIN,vehicleGraphicKind("شاهین G","خودرو سواری"))
  assertEquals(VehicleGraphicKind.MOTORCYCLE,vehicleGraphicKind("هوندا ۱۲۵","موتورسیکلت"))
  assertEquals(VehicleGraphicKind.PICKUP,vehicleGraphicKind("نیسان آبی","وانت"))
  assertEquals(VehicleGraphicKind.TRUCK,vehicleGraphicKind("ولوو","کامیون"))
 }
 @Test fun vehicleScreensUseCustomCanvasInsteadOfEmojiOrSystemCarIcon(){
  val component=File("src/main/java/ir/kharjyar/app/ui/components/VehicleGraphic.kt").readText();val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(component.contains("Canvas(")&&component.contains("quadraticBezierTo")&&component.contains("PEUGEOT_PARS"))
  assertTrue(screen.contains("VehicleGraphic(vehicle.title,vehicle.vehicleType")&&screen.contains("VehicleGraphic(title,vehicleType"))
  assertFalse(screen.contains("Text(\"🚘\""))
 }
}

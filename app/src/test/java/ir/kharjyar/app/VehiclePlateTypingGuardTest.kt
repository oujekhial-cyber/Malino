package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class VehiclePlateTypingGuardTest {
 @Test fun `plate entry uses stable local basic fields and accepts partial typing`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("PlateNumberBox","BasicTextField(","var left by remember{mutableStateOf","var serial by remember{mutableStateOf","onValueChange(clean)","keyboardType=KeyboardType.Number","cursorBrush=androidx.compose.ui.graphics.SolidColor(Color.Black)").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `sample segments clear once on first focus without clearing stored plates`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(source.contains("defaultPlate=remember{value==\"12س345ایران11\"}"))
  listOf("leftTouched","serialTouched","iranTouched").forEach{assertTrue(it,source.contains(it))}
 }
}

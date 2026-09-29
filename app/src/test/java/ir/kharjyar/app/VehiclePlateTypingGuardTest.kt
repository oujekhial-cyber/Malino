package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class VehiclePlateTypingGuardTest {
 @Test fun `partial plate typing is held locally instead of reset by incomplete parser`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(source.contains("var left by remember{mutableStateOf"))
  assertTrue(source.contains("var serial by remember{mutableStateOf"))
  assertFalse(source.contains("var left by remember(value)"))
  assertTrue(source.contains("leftFirstFocus"));assertTrue(source.contains("serialFirstFocus"));assertTrue(source.contains("iranFirstFocus"))
  assertTrue(source.contains("clearDefaultNumbers"));assertTrue(source.contains("value==\"12س345ایران11\""))
 }
}

package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class ClassicComboBoxDropdownGuardTest {
 @Test fun `combo box opens an anchored classic dropdown instead of a dialog`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/components/Pickers.kt").readText()
  val combo=source.substring(source.indexOf("fun <T> ComboBox("),source.indexOf("// ---------------------------------------------------------------- انتخابگر رنگ"))
  listOf("BoxWithConstraints","DropdownMenu(","DropdownMenuItem(","Modifier.width(maxWidth)","heightIn(max=360.dp)","open=!open","rotationZ=if(open)180f").forEach{assertTrue(it,combo.contains(it))}
  assertFalse(combo.contains("AlertDialog("))
  assertFalse(combo.contains("Text(\"بستن\""))
 }
}

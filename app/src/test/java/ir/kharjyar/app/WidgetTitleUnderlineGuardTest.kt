package ir.kharjyar.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetTitleUnderlineGuardTest {
 @Test fun `title rule sits directly below KharjYar instead of spanning widget`() {
  val panels=File("src/main/res/layout/w_panels.xml").readText()
  val split=File("src/main/res/layout/w_split.xml").readText()
  assertEquals(1,Regex("@\\+id/w_title_rule").findAll(panels).count())
  assertTrue(panels.indexOf("@+id/w_title_rule") > panels.indexOf("@+id/w_title"))
  assertFalse(panels.substringAfter("@+id/w_title_rule").substringBefore("<!-- نوار درآمد -->").contains("layout_width=\"match_parent\""))
  assertTrue(split.substringAfter("@+id/w_title").substringBefore("android:text=\"خرج‌یار\"").contains("layout_width=\"wrap_content\""))
  assertTrue(split.substringAfter("@+id/w_title_rule").contains("layout_width=\"52dp\""))
 }
}

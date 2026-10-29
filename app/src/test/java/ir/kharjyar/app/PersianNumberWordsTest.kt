package ir.kharjyar.app

import ir.kharjyar.app.core.money.PersianNumberWords
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianNumberWordsTest {
 @Test fun `writes cheque rial amounts in Persian words`() {
  assertEquals("صفر ریال",PersianNumberWords.rial(0))
  assertEquals("یکصد و بیست و سه ریال",PersianNumberWords.rial(123))
  assertEquals("یک میلیون و پانصد هزار ریال",PersianNumberWords.rial(1_500_000))
  assertEquals("یکصد میلیارد ریال",PersianNumberWords.rial(100_000_000_000))
 }
}

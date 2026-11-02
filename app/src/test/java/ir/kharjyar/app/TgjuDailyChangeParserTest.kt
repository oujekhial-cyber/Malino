package ir.kharjyar.app

import ir.kharjyar.app.assets.GoldPriceService
import org.junit.Assert.assertEquals
import org.junit.Test

class TgjuDailyChangeParserTest {
 @Test fun `daily percent is negative when current rate is below yesterday`() {
  val html="""<div>نرخ فعلی: 262,025,000</div><table><tr><td>نرخ روز گذشته</td><td>267,383,000</td></tr><tr><td>درصد تغییر نسبت به روز گذشته</td><td>2.04%</td></tr></table>"""
  assertEquals(-2.04,GoldPriceService.parseDailyChangePercent(html,"geram18")!!,0.0001)
 }
 @Test fun `daily percent is positive when current rate is above yesterday`() {
  val html="""<div>نرخ فعلی: 1,234,000</div><div>نرخ روز گذشته: 1,200,000</div><div>درصد تغییر نسبت به روز گذشته: ۲٫۸۳%</div>"""
  assertEquals(2.83,GoldPriceService.parseDailyChangePercent(html,"price_dollar_rl")!!,0.0001)
 }
}

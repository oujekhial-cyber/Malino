package ir.kharjyar.app
import ir.kharjyar.app.assets.GoldPriceService
import org.junit.Assert.assertEquals
import org.junit.Test
class MarketPriceParserTest {
 @Test fun `parses dollar rate when html tags split the visible label and number`() {
  val html="""<div>نرخ فعلی:</div><strong>2,532,000</strong><span>3.43%</span>"""
  assertEquals(2_532_000L,GoldPriceService.parseCurrentRial(html,"price_dollar_rl"))
 }
 @Test fun `parses Persian dollar digits and ignores percentage`() {
  val html="""<h3>نرخ فعلی: : <b>۲,۲۲۱,۰۰۰</b> -</h3>"""
  assertEquals(2_221_000L,GoldPriceService.parseCurrentRial(html,"price_dollar_rl"))
 }
 @Test fun `falls back to embedded JSON price`() {
  val html="""<script>{"price":"2,448,000"}</script>"""
  assertEquals(2_448_000L,GoldPriceService.parseCurrentRial(html,"price_dollar_rl"))
 }
}

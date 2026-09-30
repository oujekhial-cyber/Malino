package ir.kharjyar.app

import ir.kharjyar.app.core.sms.StockTradeSmsParser
import ir.kharjyar.app.data.db.StockSide
import org.junit.Assert.*
import org.junit.Test

class StockPortfolioGuardTest {
 @Test fun parsesCompletedBrokerTrade(){
  val p=StockTradeSmsParser.parse("AgahBroker","کارگزاری آگاه؛ معامله خرید انجام شد نماد: فولاد تعداد: ۱,۰۰۰ قیمت: ۵,۲۰۰ مبلغ کل: ۵,۲۰۰,۰۰۰ ریال")
  assertNotNull(p);assertEquals(StockSide.BUY,p!!.side);assertEquals("فولاد",p.symbol);assertEquals(1000,p.quantity);assertEquals(5_200_000,p.totalRial)
 }
 @Test fun ignoresOrdersAndBankMessages(){
  assertNull(StockTradeSmsParser.parse("Broker","سفارش خرید نماد فولاد تعداد ۱۰۰ ثبت شد"))
  assertNull(StockTradeSmsParser.parse("BANK","خرید به مبلغ ۲۰۰۰۰ ریال انجام شد"))
 }
 @Test fun migrationAndConfirmationAreExplicit(){
  val db=java.io.File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  val ui=java.io.File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  assertTrue(db.contains("MIGRATION_14_15"));assertTrue(db.contains("stock_sms_drafts"))
  assertTrue(ui.contains("این پیامک هنوز روی سبد اثر نگذاشته است"));assertTrue(ui.contains("تأیید و اعمال"))
 }
}

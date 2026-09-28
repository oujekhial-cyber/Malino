package ir.kharjyar.app

import ir.kharjyar.app.core.sms.CivicSmsClassifier
import ir.kharjyar.app.core.sms.IranianPlateMatcher
import ir.kharjyar.app.data.db.CivicMessageKind
import ir.kharjyar.app.data.db.VehicleEntity
import org.junit.Assert.*
import org.junit.Test

class IranianTrafficFinePlateTest {
 private val sample="مالک محترم، تخلف سرعت غيرمجاز(سرعت مجاز 110کيلومتربرساعت، سرعت وسيله نقليه 120 کيلومتربرساعت )براي خودرو به شماره شخصي ايران 83 ــ 519ي69 درمحورنائين اصفهان ک57 توسط دوربين با کد 604111درتاريخ 1405/07/02 14:42به مبلغ 1000000ريال ثبت گرديده است. شناسه قبض 3523500200298 شناسه پرداخت 100018895 مجموع تخلفات پرداخت نشده مبلغ 15400000 ريال مي باشد. پليس راهور فراجا"

 @Test fun `real rahvar sample is classified as traffic fine`(){assertEquals(CivicMessageKind.TRAFFIC_FINE,CivicSmsClassifier.classify("RAHVAR",sample))}
 @Test fun `extracts Iran plate despite Arabic ye spaces and dash`(){assertEquals("69|ی|519|83",IranianPlateMatcher.extract(sample)?.signature)}
 @Test fun `matches common saved plate arrangements`(){assertTrue(IranianPlateMatcher.matches(sample,"69 ی 519 ایران 83"));assertTrue(IranianPlateMatcher.matches(sample,"ایران 83 - 519ی69"));assertTrue(IranianPlateMatcher.matches(sample,"83 519 ی 69"));assertFalse(IranianPlateMatcher.matches(sample,"68 ی 519 ایران 83"))}
 @Test fun `links only a unique matching vehicle`(){val vehicles=listOf(VehicleEntity(id=7,title="خودرو من",plate="69ی519 ایران83"),VehicleEntity(id=8,title="خودرو دیگر",plate="11ب111 ایران11"));assertEquals(7,IranianPlateMatcher.uniqueVehicleId(sample,vehicles));assertNull(IranianPlateMatcher.uniqueVehicleId(sample,vehicles+VehicleEntity(id=9,title="تکراری",plate="ایران83 519ی69")))}
}

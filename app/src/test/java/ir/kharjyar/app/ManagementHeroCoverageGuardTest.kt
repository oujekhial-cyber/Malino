package ir.kharjyar.app

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ManagementHeroCoverageGuardTest {
    private fun source(name: String) = File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText()

    @Test fun chequeListAndEntryUseModernSummaryHero() {
        val text = source("ChecksScreen.kt")
        assertTrue(text.split("ModernSummaryHero(").size - 1 >= 2)
        assertTrue(text.contains("چک‌های در انتظار وصول یا پرداخت"))
        assertTrue(text.contains("اطلاعات صیادی و سررسید"))
    }

    @Test fun assetLandingCategoryAndEditorsUseModernSummaryHero() {
        val text = source("AssetsScreen.kt")
        assertTrue(text.split("ModernSummaryHero(").size - 1 >= 4)
        assertTrue(text.contains("نمای کلی سبد دارایی شما"))
        assertTrue(text.contains("ارزش و بازده این گروه"))
        assertTrue(text.contains("ثبت مشخصات، بهای خرید و ارزش روز"))
        assertTrue(text.contains("ثبت خرید جدید و محاسبه میانگین موزون"))
    }
}

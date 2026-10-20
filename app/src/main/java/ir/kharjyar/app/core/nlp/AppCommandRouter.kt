package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.text.Digits

/** مسیریاب قطعی و آفلاین فرمان‌ها؛ عبارت‌های مسیریابی هیچ‌گاه به سرویس آنلاین فرستاده نمی‌شوند. */
object AppCommandRouter {
    private val commands = listOf(
        listOf("تراکنش", "برداشت ها", "واریز ها", "لیست کن", "نمایش بده") to "transactions",
        listOf("گزارش", "آمار", "تحلیل مالی") to "reports",
        listOf("حساب ها", "حساب بانکی", "کیف پول") to "accounts",
        listOf("دسته بندی") to "categories",
        listOf("بدهی", "طلب") to "debts",
        listOf("وام", "قسط", "اقساط") to "loans",
        listOf("چک") to "checks",
        listOf("دارایی", "طلا", "ارز", "سکه") to "assets",
        listOf("خودرو", "ماشین") to "vehicles",
        listOf("یادآوری") to "reminders",
        listOf("یادداشت") to "notes",
        listOf("پیامک بانکی", "پیامک ها") to "review",
        listOf("بازار", "قیمت طلا", "قیمت دلار") to "marketPulse",
        listOf("پشتیبان", "بکاپ") to "backup",
        listOf("تنظیمات") to "settings"
    )

    fun featureAction(text: String): Pair<String,String>? {
        val value=Digits.normalizeForMatch(text).replace('‌',' ')
        val registration=listOf("ثبت کن","ثبت کردم","خریدم","فروختم","یادآوری کن","یادم بنداز","صادر کردم","دریافت کردم","پرداخت کردم").any(value::contains)
        if(!registration)return null
        val vehicleWords=listOf("خودرو","ماشین","اتومبیل","موتورسیکلت","موتور سیکلت","وانت","کامیون","کامیونت","تریلی","اتوبوس","مینی بوس","وسیله نقلیه")
        val propertyWords=listOf("ملک","املاک","خانه","خونه","منزل","آپارتمان","زمین","ویلا","باغ","مغازه","دفتر کار","واحد تجاری","مسکن")
        val metalWords=listOf("طلا","زر","سکه","نقره","پلاتین","شمش")
        val currencyWords=listOf("ارز","دلار","یورو","پوند","درهم","لیر","فرانک","یوان")
        val stockWords=listOf("سهام","بورس","سهم")
        val otherAssetWords=listOf("دارایی","موبایل","گوشی","لپ تاپ","لپتاپ","کامپیوتر","تبلت","ساعت","دوچرخه","لوازم خانگی","کلکسیون")
        return when {
            // وسیله نقلیه بخش مستقل دارد و نباید به «سایر دارایی‌ها» فرستاده شود.
            vehicleWords.any(value::contains)->"vehicle_record" to "vehicles"
            (propertyWords+metalWords+currencyWords+stockWords+otherAssetWords).any(value::contains)->"asset_record" to "assets"
            value.contains("یادآور")||value.contains("یادم بنداز")->"reminder_create" to "reminders"
            value.contains("چک")&&listOf("صادر","دادم").any(value::contains)->"check_issued" to "checks"
            value.contains("چک")->"check_received" to "checks"
            value.contains("یادداشت")->"note_create" to "notes"
            value.contains("قسط")&&value.contains("پرداخت")->"installment_payment" to "loans"
            listOf("قبض","جریمه").any(value::contains)->"civic_record" to "civicCenter"
            else->null
        }
    }

    fun obligationAction(text: String): String? {
        val value = Digits.normalizeForMatch(text).replace('‌',' ')
        return when {
            (value.contains("وام") && listOf("گرفتم","دریافتی","دریافت کردم").any(value::contains)) -> "loan_borrowed"
            (value.contains("وام") && listOf("دادم","پرداختی","وام دادم").any(value::contains)) -> "loan_lent"
            (value.contains("قرض") && listOf("دادم","قرض دادم").any(value::contains)) -> "debt_receivable"
            (value.contains("قرض") && listOf("گرفتم","قرض گرفتم").any(value::contains)) -> "debt_payable"
            else -> null
        }
    }

    fun route(text: String): String? {
        val normalized = Digits.normalizeForMatch(text).replace('‌',' ')
        val navigationIntent = listOf("باز کن", "برو", "نشان بده", "نمایش بده", "لیست کن", "فهرست").any(normalized::contains)
        if (!navigationIntent) return null
        return commands.firstOrNull { (words, _) -> words.any(normalized::contains) }?.second
    }
}

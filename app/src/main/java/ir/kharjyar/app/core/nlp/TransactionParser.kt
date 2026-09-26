package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature

/** یک حساب، آن‌قدر که پارسر لازم دارد. */
data class ParserAccount(val id: Long, val title: String, val bankName: String)

/** یک دسته، آن‌قدر که پارسر لازم دارد. */
data class ParserCategory(val id: Long, val name: String)

/** میزان اطمینان پارسر به نتیجه. */
enum class ParseConfidence { HIGH, MEDIUM, LOW }

/**
 * نتیجه تحلیل جمله کاربر.
 *
 * هیچ‌وقت مستقیم ثبت نمی‌شود؛ همیشه اول به کاربر نشان داده می‌شود تا تأیید کند.
 */
data class ParsedTransaction(
    val amountRial: Long?,
    val direction: Int,
    val nature: Int,
    val accountId: Long?,
    val accountTitle: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val description: String,
    val date: PersianDate,
    val hour: Int,
    val minute: Int,
    /** آیا کاربر تاریخ را صریح گفته یا پیش‌فرض «امروز» گذاشته‌ایم. */
    val dateExplicit: Boolean,
    val confidence: ParseConfidence,
    /** مواردی که پارسر مطمئن نیست و بهتر است کاربر بررسی کند. */
    val warnings: List<String>,
    /** حساب مقصد، فقط برای انتقال بین حساب‌های خود کاربر. */
    val targetAccountId: Long? = null,
    val targetAccountTitle: String? = null,
    val transferToOwn: Boolean = false
) {
    /** بدون مبلغ یا بدون حساب نمی‌توان ثبت کرد. */
    val isComplete: Boolean get() = amountRial != null && amountRial > 0 && accountId != null
}

/**
 * تحلیل‌گر جمله فارسی برای ثبت سریع تراکنش.
 *
 * تماماً روی خود گوشی اجرا می‌شود؛ هیچ داده‌ای به بیرون نمی‌رود و برنامه
 * همچنان بدون مجوز اینترنت کار می‌کند.
 *
 * نمونه ورودی:
 *   «۲۵۰ هزار تومن کیک از سوپرمارکت خریدم با حساب روزمره»
 */
object TransactionParser {

    // ---------- واژگان ----------

    /** فعل‌ها و کلماتی که یعنی پول از حساب خارج شده. */
    private val withdrawalWords = listOf(
        "خریدم", "خرید کردم", "خرید", "دادم", "پول دادم", "پرداخت کردم", "پرداخت شد",
        "پرداخت", "پرداختم", "هزینه کردم", "هزینه شد", "هزینه", "خرج کردم", "خرج شد", "خرج",
        "برداشت کردم", "برداشت شد", "برداشتم", "برداشت", "کشیدم", "کارت کشیدم",
        "حساب کردم", "تسویه کردم", "رد کردم", "فرستادم", "ارسال کردم", "ارسال",
        "واریز کردم به", "قبض دادم", "کرایه دادم", "قسط دادم", "کارمزد کم شد"
    )

    /** فعل‌ها و کلماتی که یعنی پول وارد حساب شده. */
    private val depositWords = listOf(
        "گرفتم", "پول گرفتم", "دریافت کردم", "دریافت شد", "دریافت", "دریافتی",
        "واریز شد", "واریز کردند", "واریز کردن", "به حسابم آمد", "به حسابم اومد",
        "ریختن", "ریخت", "ریختند", "نشست به حساب", "بستانکار شد",
        "حقوق گرفتم", "حقوق", "درآمد داشتم", "درآمد", "فروختم", "فروش داشتم",
        "پس گرفتم", "برگشت خورد", "عودت شد", "بهم دادن", "بهم داد", "عیدی", "پاداش", "سود"
    )

    /** انتقال بین حساب‌های خود کاربر. */
    private val transferWords = listOf(
        "انتقال", "انتقال دادم", "انتقال زدم", "منتقل", "منتقل کردم",
        "جابجا", "جابجا کردم", "جابه جا", "جابه‌جا", "جابه‌جا کردم",
        "کارت به کارت", "کارت‌به‌کارت", "کارت به کارت کردم", "بین حساب",
        "واریز کردم به", "حواله کردم"
    )

    /**
     * فرهنگ عبارت‌های انتقال؛ ترکیب این فهرست‌ها در تست corpus صدها جمله
     * طبیعی می‌سازد تا تفاوت لحن، فعل و پیشوند نام حساب پوشش داده شود.
     */
    private val transferSourceMarkers = listOf(
        "از حساب", "از کارت", "از توی حساب", "از داخل حساب", "از سپرده", "مبدا", "مبدأ", "از "
    )
    private val transferTargetMarkers = listOf(
        "به حساب", "به کارت", "به حساب خودم", "به حساب دیگه خودم", "به سپرده", "مقصد", "به "
    )
    private val otherPeopleWords = listOf(
        "دیگران", "شخص دیگر", "حساب دیگری", "حساب فرد دیگر", "حساب دوستم",
        "برای کسی", "به کسی", "برای دوستم", "به دوستم", "به فروشنده"
    )

    /** واحد پول در متن. */
    private val tomanWords = listOf("تومان", "تومن", "توما")
    private val rialWords = listOf("ریال")

    /** کلیدواژه هر دسته پیش‌فرض، برای حدس زدن دسته‌بندی. */
    private val categoryHints: Map<String, List<String>> = mapOf(
        "خوراک و سوپرمارکت" to listOf(
            "سوپرمارکت", "سوپر", "خواربار", "نان", "نانوایی", "میوه", "تره‌بار",
            "گوشت", "مرغ", "لبنیات", "شیر", "کیک", "شیرینی", "قنادی", "خوراک", "بقالی"
        ),
        "رستوران و کافه" to listOf("رستوران", "کافه", "کافی‌شاپ", "فست‌فود", "پیتزا", "ساندویچ", "قهوه", "چایخانه", "غذا"),
        "حمل‌ونقل (خودرو)" to listOf("تاکسی", "اسنپ", "تپسی", "مترو", "اتوبوس", "بنزین", "سوخت", "گازوئیل", "کرایه", "بلیت", "پارکینگ"),
        "مسکن و اجاره" to listOf("اجاره", "رهن", "ودیعه", "شارژ ساختمان", "مسکن"),
        "قبوض" to listOf("قبض", "برق", "آب", "گاز", "عوارض", "جریمه"),
        "اینترنت و تلفن" to listOf("اینترنت", "شارژ", "بسته", "همراه اول", "ایرانسل", "رایتل", "مخابرات", "تلفن", "سیم‌کارت"),
        "درمان و سلامت" to listOf("دکتر", "پزشک", "دارو", "داروخانه", "بیمارستان", "آزمایش", "دندان", "درمان", "ویزیت"),
        "پوشاک" to listOf("لباس", "کفش", "پوشاک", "مانتو", "شلوار", "پیراهن"),
        "آموزش" to listOf("کلاس", "آموزش", "دانشگاه", "مدرسه", "کتاب", "شهریه", "دوره"),
        "تفریح" to listOf("سینما", "تفریح", "سفر", "بازی", "کنسرت", "استخر", "باشگاه"),
        "خرید آنلاین" to listOf("دیجی‌کالا", "دیجیکالا", "اینترنتی", "آنلاین", "باسلام", "ترب"),
        "هدیه و کمک" to listOf("هدیه", "کادو", "کمک", "صدقه", "خیریه", "عیدی"),
        "کارمزد بانکی" to listOf("کارمزد", "کارمزد بانکی", "هزینه تراکنش"),
        "حقوق" to listOf("حقوق", "دستمزد", "مواجب", "پاداش")
    )

    /** روزهای هفته برای تاریخ نسبی. ایندکس صفر = شنبه. */
    private val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "سه شنبه", "چهارشنبه", "پنجشنبه", "پنج‌شنبه", "جمعه")

    // ---------- ورودی اصلی ----------

    /**
     * جمله کاربر را تحلیل می‌کند.
     *
     * @param text جمله فارسی، تایپی یا حاصل تبدیل گفتار به متن.
     * @param accounts حساب‌های کاربر، برای تطبیق نام.
     * @param categories دسته‌های موجود.
     * @param defaultUnit واحدی که اگر کاربر نگفت فرض شود.
     * @param today امروز؛ برای تست‌پذیری قابل تزریق است.
     */
    fun parse(
        text: String,
        accounts: List<ParserAccount>,
        categories: List<ParserCategory>,
        defaultUnit: MoneyUnit,
        today: PersianDate = PersianDate.today()
    ): ParsedTransaction {
        val warnings = mutableListOf<String>()
        val normalized = normalize(text)

        // ---------- مبلغ ----------
        val rawAmount = PersianNumbers.parseFirst(normalized)
        val unit = detectUnit(normalized, defaultUnit)
        val amountRial = rawAmount?.let {
            if (unit == MoneyUnit.TOMAN) it * 10 else it
        }
        if (amountRial == null) warnings.add("مبلغ پیدا نشد")

        // ---------- جهت و ماهیت ----------
        val hasTransferPath = transferSourceMarkers.any { normalized.contains(it) } &&
            transferTargetMarkers.any { normalized.contains(it) }
        val isTransfer = transferWords.any { normalized.contains(it) } || hasTransferPath
        val depositHit = depositWords.any { normalized.contains(it) }
        val withdrawalHit = withdrawalWords.any { normalized.contains(it) }

        val direction = when {
            isTransfer -> TxDirection.WITHDRAW
            depositHit && !withdrawalHit -> TxDirection.DEPOSIT
            withdrawalHit -> TxDirection.WITHDRAW
            else -> TxDirection.WITHDRAW   // حالت رایج‌تر
        }
        val nature = when {
            isTransfer -> TxNature.TRANSFER
            direction == TxDirection.DEPOSIT -> TxNature.INCOME
            else -> TxNature.EXPENSE
        }
        if (!depositHit && !withdrawalHit && !isTransfer) {
            warnings.add("نوع تراکنش مشخص نبود؛ «برداشت» در نظر گرفته شد")
        }

        // ---------- حساب ----------
        val sourceAccount = if (isTransfer) {
            matchAccountNear(normalized, accounts, transferSourceMarkers)
                ?: matchAccount(normalized, accounts)
        } else matchAccount(normalized, accounts)
        val targetAccount = if (isTransfer) {
            matchAccountNear(normalized, accounts, transferTargetMarkers, excludeId = sourceAccount?.id)
        } else null
        val transferToOwn = isTransfer && targetAccount != null
        val account = sourceAccount
        if (account == null) {
            warnings.add(
                if (accounts.isEmpty()) "هنوز حسابی ثبت نکرده‌اید"
                else "حساب مشخص نشد؛ انتخابش کنید"
            )
        }

        if (isTransfer && targetAccount == null &&
            !otherPeopleWords.any { normalized.contains(it) }
        ) {
            warnings.add("مقصد انتقال مشخص نشد؛ انتقال به حساب دیگران در نظر گرفته شد")
        }

        // ---------- تاریخ ----------
        val (date, explicit) = matchDate(normalized, today)

        // ---------- دسته ----------
        val category = matchCategory(normalized, categories, nature)

        // ---------- شرح ----------
        val description = buildDescription(
            original = text,
            normalized = normalized,
            account = account,
            targetAccount = targetAccount,
            category = category
        )

        val confidence = when {
            amountRial != null && account != null && warnings.isEmpty() -> ParseConfidence.HIGH
            amountRial != null && account != null -> ParseConfidence.MEDIUM
            else -> ParseConfidence.LOW
        }

        return ParsedTransaction(
            amountRial = amountRial,
            direction = direction,
            nature = nature,
            accountId = account?.id,
            accountTitle = account?.title,
            categoryId = category?.id,
            categoryName = category?.name,
            description = description,
            date = date,
            hour = PersianDate.nowHourMinute().first,
            minute = PersianDate.nowHourMinute().second,
            dateExplicit = explicit,
            confidence = confidence,
            warnings = warnings,
            targetAccountId = targetAccount?.id,
            targetAccountTitle = targetAccount?.title,
            transferToOwn = transferToOwn
        )
    }

    // ---------- بخش‌های کمکی ----------

    private fun normalize(text: String): String =
        Digits.normalize(text)
            .replace('\u200c', ' ')
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace(Regex("[,،٬]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun detectUnit(text: String, default: MoneyUnit): MoneyUnit = when {
        tomanWords.any { text.contains(it) } -> MoneyUnit.TOMAN
        rialWords.any { text.contains(it) } -> MoneyUnit.RIAL
        else -> default
    }

    /**
     * تطبیق نام حساب.
     * اول دنبال عنوان دقیق می‌گردد، بعد نام بانک، و در آخر واژه‌به‌واژه.
     */
    private fun matchAccount(text: String, accounts: List<ParserAccount>): ParserAccount? {
        if (accounts.isEmpty()) return null

        // عنوان کامل یا ساده‌شده حساب در جمله آمده باشد
        accounts.sortedByDescending { it.title.length }.forEach { acc ->
            if (accountAliases(acc).any { text.contains(it) }) return acc
        }
        // نام بانک، اگر فقط یک حساب از آن بانک باشد
        accounts.sortedByDescending { it.bankName.length }.forEach { acc ->
            val b = normalize(acc.bankName)
            if (b.isNotBlank() && text.contains(b)) {
                val sameBank = accounts.count { normalize(it.bankName) == b }
                if (sameBank == 1) return acc
            }
        }
        // تطبیق واژه‌ای: دست‌کم یک واژه سه‌حرفی مشترک
        val words = text.split(" ").filter { it.length >= 3 }.toSet()
        accounts.forEach { acc ->
            val titleWords = normalize(acc.title).split(" ").filter { it.length >= 3 }
            if (titleWords.any { it in words }) return acc
        }
        return null
    }

    /** نام‌های قابل تطبیق حساب: عنوان/بانک، با و بدون پیشوندهای رایج. */
    private fun accountAliases(account: ParserAccount): List<String> =
        listOf(account.title, account.bankName)
            .map(::normalize)
            .flatMap { value ->
                listOf(
                    value,
                    value.removePrefix("حساب ").removePrefix("بانک ").trim(),
                    value.removeSuffix(" خودم").trim()
                )
            }
            .filter { it.length >= 2 }
            .distinct()
            .sortedByDescending { it.length }

    /** حسابی که بعد از یکی از عبارت‌های «از …» یا «به …» آمده است. */
    private fun matchAccountNear(
        text: String,
        accounts: List<ParserAccount>,
        markers: List<String>,
        excludeId: Long? = null
    ): ParserAccount? {
        val candidates = accounts.filter { it.id != excludeId }
        for (marker in markers.sortedByDescending { it.length }) {
            var start = 0
            while (true) {
                val index = text.indexOf(marker, start)
                if (index < 0) break
                val window = text.substring(index + marker.length).take(50)
                // همه حساب‌ها را امتیاز می‌دهیم و نزدیک‌ترین نام بعد از marker
                // را برمی‌گزینیم. قبلاً اولین حسابِ لیست که جایی در پنجره ۵۰
                // نویسه‌ای دیده می‌شد برنده بود؛ چون پنجره هر دو نام مبدأ و مقصد
                // را داشت، گاهی مقصد با اینکه نوشته شده بود تشخیص داده نمی‌شد.
                val best = candidates.mapNotNull { account ->
                    val positions = accountAliases(account)
                        .map { alias -> window.indexOf(alias) }
                        .filter { it >= 0 }
                    positions.minOrNull()?.let { position -> account to position }
                }.minByOrNull { (_, position) -> position }
                if (best != null) return best.first
                start = index + marker.length
            }
        }
        return null
    }

    /**
     * تاریخ نسبی. خروجی دوم می‌گوید کاربر صریح گفته یا پیش‌فرض گذاشته‌ایم.
     */
    private fun matchDate(text: String, today: PersianDate): Pair<PersianDate, Boolean> {
        when {
            text.contains("پریروز") -> return today.plusDays(-2) to true
            text.contains("دیروز") -> return today.plusDays(-1) to true
            text.contains("پس فردا") || text.contains("پس‌فردا") -> return today.plusDays(2) to true
            text.contains("فردا") -> return today.plusDays(1) to true
            text.contains("امروز") -> return today to true
        }
        // «سه‌شنبه» یا «سه‌شنبه گذشته» → نزدیک‌ترین روز گذشته با آن نام
        weekDays.forEachIndexed { _, name ->
            if (text.contains(name)) {
                for (back in 1..7) {
                    val d = today.plusDays(-back)
                    if (d.dayOfWeekName() == name.replace(" ", "\u200c")) return d to true
                }
            }
        }
        return today to false
    }

    /** حدس دسته از روی کلیدواژه‌ها، محدود به دسته‌های موجود کاربر. */
    private fun matchCategory(
        text: String,
        categories: List<ParserCategory>,
        nature: Int
    ): ParserCategory? {
        if (categories.isEmpty()) return null
        // دسته‌ای که نامش مستقیم در جمله آمده
        categories.sortedByDescending { it.name.length }.forEach { c ->
            if (text.contains(normalize(c.name))) return c
        }
        // از روی کلیدواژه‌ها
        categoryHints.forEach { (catName, hints) ->
            if (hints.any { text.contains(it) }) {
                categories.firstOrNull {
                    val existing = normalize(it.name)
                    val expected = normalize(catName)
                    existing == expected || existing.substringBefore(" (") == expected.substringBefore(" (")
                }?.let { return it }
            }
        }
        // انتقال دسته ندارد
        if (nature == TxNature.TRANSFER) return null
        return null
    }

    /**
     * شرح: جمله کاربر با حذف بخش‌های ساختاری (مبلغ، واحد، نام حساب).
     * اگر چیز معناداری نماند، خود جمله اصلی برمی‌گردد.
     */
    private fun buildDescription(
        original: String,
        normalized: String,
        account: ParserAccount?,
        targetAccount: ParserAccount?,
        category: ParserCategory?
    ): String {
        var s = normalized
        // حذف مبلغ و واحد
        s = s.replace(Regex("\\d+(?:[.,]\\d+)*"), " ")
        (tomanWords + rialWords + listOf("هزار", "میلیون", "میلیارد", "نیم")).forEach {
            s = s.replace(it, " ")
        }
        // عنوان و نام بانک حساب، و نام دسته نباید وارد شرح شوند
        listOfNotNull(
            account?.title, account?.bankName,
            targetAccount?.title, targetAccount?.bankName,
            category?.name
        ).map(::normalize)
            .filter { it.isNotBlank() }
            .sortedByDescending { it.length }
            .forEach { s = s.replace(it, " ") }
        // اجزای دستوری جمله؛ فقط موضوع واقعی خرید/واریز باقی بماند
        val structural = listOf(
            "با حساب", "از حساب", "به حساب", "حساب", "کارت", "بانک",
            "خریدم", "خرید کردم", "خرید", "پرداختم", "پرداخت کردم", "پرداخت",
            "دادم", "گرفتم", "دریافت کردم", "واریز شد", "واریز کردم", "واریز",
            "انتقال وجه دادم", "انتقال وجه کردم", "انتقال دادم", "منتقل کردم", "انتقال وجه", "انتقال",
            "بابت", "امروز", "دیروز", "پریروز", "فردا", "این ماه", "این هفته"
        )
        structural.sortedByDescending { it.length }.forEach { s = s.replace(it, " ") }
        s = s.replace(Regex("\\s+"), " ").trim(' ', '،', ',', '-', '_')
        // شرح کوتاه و معنادار؛ اگر چیزی نماند، شرح را خالی می‌گذاریم نه اینکه
        // جمله ساختاری و بی‌معنی اولیه را دوباره نمایش دهیم.
        return if (s.length >= 2) s else ""
    }
}

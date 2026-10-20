package ir.kharjyar.app.ui.screens

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits

/** تبدیل امن اطلاعات گفتاری/تایپی چک به فیلدهای فرم، بدون حدس‌زدن داده مالی. */
internal fun parseCheckDraftDate(aiDate: String?, raw: String?): PersianDate? {
    val normalized = Digits.normalize(aiDate.orEmpty()).ifBlank { Digits.normalize(raw.orEmpty()) }
    val match = Regex("(1[34]\\d{2})[\\/\\-.](0?[1-9]|1[0-2])[\\/\\-.](0?[1-9]|[12]\\d|3[01])").find(normalized) ?: return null
    val (year, month, day) = match.destructured
    return runCatching { PersianDate(year.toInt(), month.toInt(), day.toInt()) }
        .getOrNull()?.takeIf { it.day <= it.monthLength() }
}

internal fun parseCheckBank(raw: String): String {
    val value = Digits.normalizeForMatch(raw)
    return listOf("بانک ملی","بانک ملت","بانک صادرات","بانک تجارت","بانک سپه","بانک رفاه","بانک مسکن","بانک کشاورزی","بانک پاسارگاد","بانک سامان","بانک پارسیان","بانک آینده","بانک شهر","بانک دی","بانک سینا","بانک اقتصاد نوین","بانک گردشگری","بانک رسالت","بانک مهر ایران")
        .firstOrNull { value.contains(Digits.normalizeForMatch(it)) }.orEmpty()
}

internal fun parseCheckParty(raw: String): String {
    val value = raw.trim()
    return Regex("(?:از|برای|به نام)\\s+([\\p{L}‌ ]{2,40}?)(?=\\s+(?:به مبلغ|مبلغ|سررسید|بانک|با شناسه|شناسه)|$)")
        .find(value)?.groupValues?.getOrNull(1)?.trim().orEmpty()
}

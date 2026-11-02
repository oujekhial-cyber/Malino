package ir.kharjyar.app.core.backup

import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsStatus

/**
 * ادغام صف «نیازمند بررسی» هنگام بازیابی بکاپ.
 *
 * مشکلی که این کلاس حل می‌کند: بازیابی، جدول‌ها را کامل پاک می‌کرد و پیامک‌هایی
 * که بعد از گرفتن بکاپ وارد صف بررسی شده بودند از بین می‌رفتند. آن پیامک‌ها هنوز
 * تبدیل به تراکنش نشده‌اند و در هیچ بکاپی هم نیستند، یعنی حذفشان یعنی از دست رفتن داده.
 *
 * قاعده ادغام:
 *  ۱. فقط موارد «بررسی‌نشده» نگه داشته می‌شوند (تأییدشده/صرف‌نظرشده جزو همان
 *     وضعیتی‌اند که بکاپ توصیف می‌کند و نباید با بکاپ قاطی شوند).
 *  ۲. کلید یکتایی `fingerprint` است؛ اگر همان پیامک داخل بکاپ هم باشد، نسخه بکاپ
 *     می‌ماند و مورد فعلی دور ریخته می‌شود تا در صف تکراری نشود.
 *  ۳. شناسه‌های تکراری با بکاپ، شناسه تازه می‌گیرند تا رکورد بکاپ را بازنویسی نکنند.
 *  ۴. ارجاع‌های مرده پاک می‌شوند: حساب/قالبی که در بکاپ نیست دیگر وجود نخواهد داشت.
 *     «پیش‌نویس آماده» هم به RAW برمی‌گردد، چون تراکنش پیش‌نویسش با بازیابی رفته و
 *     کاربر می‌تواند با یک لمس دوباره پردازشش کند.
 *
 * منطق اینجا خالص و بدون وابستگی به اندروید است تا تست واحد داشته باشد.
 */
object SmsQueueMerge {

    /** وضعیت‌هایی که یعنی «کاربر هنوز تعیین تکلیف نکرده». همان صف صفحه بررسی. */
    val UNREVIEWED_STATUSES = listOf(
        SmsStatus.RAW,
        SmsStatus.NEEDS_ACCOUNT,
        SmsStatus.NEEDS_TEMPLATE,
        SmsStatus.DRAFT_READY
    )

    fun isUnreviewed(status: Int): Boolean = status in UNREVIEWED_STATUSES

    /**
     * @param carriedOver رکوردهایی که باید بعد از درج صف بکاپ درج شوند.
     * @param duplicates تعداد مواردی که چون در بکاپ هم بودند نگه داشته نشدند.
     */
    data class MergeResult(
        val carriedOver: List<SmsCandidateEntity>,
        val duplicates: Int
    )

    /**
     * @param backupQueue صف داخل فایل بکاپ (همان چیزی که قرار است درج شود).
     * @param currentQueue صف فعلی دیتابیس (قبل از پاک‌سازی خوانده می‌شود).
     * @param knownAccountIds شناسه حساب‌های موجود در بکاپ.
     * @param knownTemplateIds شناسه قالب‌های موجود در بکاپ.
     */
    fun merge(
        backupQueue: List<SmsCandidateEntity>,
        currentQueue: List<SmsCandidateEntity>,
        knownAccountIds: Set<Long> = emptySet(),
        knownTemplateIds: Set<Long> = emptySet()
    ): MergeResult {
        val backupFingerprints = backupQueue.map { it.fingerprint }.filter { it.isNotBlank() }.toSet()
        val usedIds = backupQueue.mapTo(mutableSetOf()) { it.id }
        var nextId = ((backupQueue.map { it.id } + currentQueue.map { it.id }).maxOrNull() ?: 0L) + 1L

        val seenFingerprints = mutableSetOf<String>()
        val carried = mutableListOf<SmsCandidateEntity>()
        var duplicates = 0

        currentQueue.asSequence()
            .filter { isUnreviewed(it.status) }
            .sortedBy { it.id }
            .forEach { sms ->
                val fp = sms.fingerprint
                if (fp.isNotBlank() && (fp in backupFingerprints || !seenFingerprints.add(fp))) {
                    duplicates++
                    return@forEach
                }
                val id: Long
                if (usedIds.add(sms.id)) {
                    id = sms.id
                } else {
                    while (!usedIds.add(nextId)) nextId++
                    id = nextId
                    nextId++
                }
                carried += sanitize(sms.copy(id = id), knownAccountIds, knownTemplateIds)
            }

        return MergeResult(carried, duplicates)
    }

    /** ارجاع‌های نامعتبر پس از جایگزینی داده‌ها پاک می‌شوند تا صف بررسی نشکند. */
    private fun sanitize(
        sms: SmsCandidateEntity,
        knownAccountIds: Set<Long>,
        knownTemplateIds: Set<Long>
    ): SmsCandidateEntity {
        val account = sms.matchedAccountId?.takeIf { it in knownAccountIds }
        val template = sms.matchedTemplateId?.takeIf { it in knownTemplateIds }
        val status = when {
            // تراکنش پیش‌نویس با بازیابی حذف شده؛ پیامک باید دوباره پردازش شود
            sms.status == SmsStatus.DRAFT_READY -> SmsStatus.RAW
            sms.status == SmsStatus.NEEDS_TEMPLATE && account == null -> SmsStatus.RAW
            else -> sms.status
        }
        return sms.copy(
            status = status,
            matchedAccountId = account,
            matchedTemplateId = template,
            extractionJson = if (status == SmsStatus.RAW) null else sms.extractionJson
        )
    }
}

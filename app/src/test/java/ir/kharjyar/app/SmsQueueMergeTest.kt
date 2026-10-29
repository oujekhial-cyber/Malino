package ir.kharjyar.app

import ir.kharjyar.app.core.backup.SmsQueueMerge
import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * باگ: بازیابی بکاپ صف «نیازمند بررسی» را کامل پاک می‌کرد و پیامک‌های
 * بعد از بکاپ برای همیشه از بین می‌رفتند.
 */
class SmsQueueMergeTest {

    private fun sms(
        id: Long,
        fingerprint: String,
        status: Int = SmsStatus.NEEDS_ACCOUNT,
        accountId: Long? = null,
        templateId: Long? = null,
        extraction: String? = null
    ) = SmsCandidateEntity(
        id = id,
        sender = "BANK",
        body = "متن پیامک $fingerprint",
        receivedAt = 1_000L + id,
        fingerprint = fingerprint,
        status = status,
        matchedAccountId = accountId,
        matchedTemplateId = templateId,
        extractionJson = extraction,
        updatedAt = 2_000L
    )

    @Test
    fun `unreviewed sms survives a restore`() {
        val backup = listOf(sms(1, "fp-old", status = SmsStatus.DONE))
        val current = listOf(
            sms(1, "fp-old", status = SmsStatus.DONE),
            sms(2, "fp-new")
        )

        val result = SmsQueueMerge.merge(backup, current)

        assertEquals(1, result.carriedOver.size)
        assertEquals("fp-new", result.carriedOver.first().fingerprint)
    }

    @Test
    fun `reviewed sms is not carried over`() {
        val current = listOf(
            sms(5, "fp-done", status = SmsStatus.DONE),
            sms(6, "fp-dismissed", status = SmsStatus.DISMISSED),
            sms(7, "fp-nonfinancial", status = SmsStatus.NON_FINANCIAL)
        )

        val result = SmsQueueMerge.merge(emptyList(), current)

        assertTrue("فقط موارد بررسی‌نشده باید نگه داشته شوند", result.carriedOver.isEmpty())
    }

    @Test
    fun `same fingerprint is not duplicated in the queue`() {
        val backup = listOf(sms(10, "fp-same", status = SmsStatus.NEEDS_ACCOUNT))
        val current = listOf(sms(99, "fp-same", status = SmsStatus.NEEDS_ACCOUNT))

        val result = SmsQueueMerge.merge(backup, current)

        assertTrue(result.carriedOver.isEmpty())
        assertEquals(1, result.duplicates)
    }

    @Test
    fun `colliding ids are reassigned so backup rows are not overwritten`() {
        val backup = listOf(sms(1, "fp-a"), sms(2, "fp-b"))
        val current = listOf(sms(2, "fp-kept"))

        val result = SmsQueueMerge.merge(backup, current)

        val carried = result.carriedOver.single()
        assertEquals("fp-kept", carried.fingerprint)
        assertTrue("شناسه نباید با رکورد بکاپ یکی باشد", carried.id !in listOf(1L, 2L))
    }

    @Test
    fun `free id is kept as is`() {
        val backup = listOf(sms(1, "fp-a"))
        val current = listOf(sms(7, "fp-kept"))

        val carried = SmsQueueMerge.merge(backup, current).carriedOver.single()

        assertEquals(7L, carried.id)
    }

    @Test
    fun `draft goes back to raw because its draft transaction is gone`() {
        val current = listOf(
            sms(3, "fp-draft", status = SmsStatus.DRAFT_READY, accountId = 1, extraction = "{}")
        )

        val carried = SmsQueueMerge.merge(
            backupQueue = emptyList(),
            currentQueue = current,
            knownAccountIds = setOf(1L)
        ).carriedOver.single()

        assertEquals(SmsStatus.RAW, carried.status)
        assertNull("نتیجه استخراج قدیمی باید پاک شود", carried.extractionJson)
    }

    @Test
    fun `dead references to accounts and templates are cleared`() {
        val current = listOf(
            sms(4, "fp-tpl", status = SmsStatus.NEEDS_TEMPLATE, accountId = 42, templateId = 7)
        )

        val carried = SmsQueueMerge.merge(
            backupQueue = emptyList(),
            currentQueue = current,
            knownAccountIds = setOf(1L),
            knownTemplateIds = setOf(2L)
        ).carriedOver.single()

        assertNull(carried.matchedAccountId)
        assertNull(carried.matchedTemplateId)
        assertEquals("بدون حساب معتبر باید دوباره پردازش شود", SmsStatus.RAW, carried.status)
    }

    @Test
    fun `valid account reference is preserved`() {
        val current = listOf(
            sms(4, "fp-tpl", status = SmsStatus.NEEDS_TEMPLATE, accountId = 1, templateId = 2)
        )

        val carried = SmsQueueMerge.merge(
            backupQueue = emptyList(),
            currentQueue = current,
            knownAccountIds = setOf(1L),
            knownTemplateIds = setOf(2L)
        ).carriedOver.single()

        assertEquals(1L, carried.matchedAccountId)
        assertEquals(2L, carried.matchedTemplateId)
        assertEquals(SmsStatus.NEEDS_TEMPLATE, carried.status)
    }

    @Test
    fun `restore still wipes the queue when nothing is pending`() {
        val backup = listOf(sms(1, "fp-a"), sms(2, "fp-b"))

        val result = SmsQueueMerge.merge(backup, emptyList())

        assertTrue(result.carriedOver.isEmpty())
        assertEquals(0, result.duplicates)
    }
}

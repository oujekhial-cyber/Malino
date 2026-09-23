package ir.kharjyar.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** حساب بانکی کاربر. */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val bankName: String,
    val colorArgb: Long = 0xFF3F51B5,
    val icon: String = "bank",
    /** شناسه ماسک‌شده کارت/حساب برای نمایش، مثل ****1234 */
    val maskedNumber: String = "",
    /** شماره حساب. خالی یعنی کاربر وارد نکرده و نباید نمایش داده شود. */
    val accountNumber: String = "",
    /** شماره شبا بدون IR. */
    val iban: String = "",
    /** شماره ۱۶ رقمی کارت. */
    val cardNumber: String = "",
    /** تاریخ انقضای کارت به شکل MM/YY. */
    val cardExpiry: String = "",
    /** CVV2 کارت. */
    val cardCvv2: String = "",
    /** موجودی اولیه اختیاری (ریال). */
    val initialBalanceRial: Long? = null,
    /** زمان ثبت موجودی اولیه. */
    val initialBalanceAt: Long? = null,
    val archived: Boolean = false,
    val createdAt: Long
)

/** نگاشت فرستنده پیامک + شناسه داخل متن به حساب. */
@Entity(
    tableName = "account_senders",
    indices = [Index("accountId"), Index("sender")]
)
data class AccountSenderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    /** سرشماره فرستنده، مثل +9850002level or "700701" */
    val sender: String,
    /** بخشی از شناسه حساب/کارت که در متن پیامک می‌آید؛ خالی یعنی هر پیامک این فرستنده. */
    val identifierHint: String = ""
)

/** وضعیت پیامک دریافتی. */
object SmsStatus {
    const val RAW = 0            // تازه دریافت شده، هنوز پردازش نشده
    const val NON_FINANCIAL = 1  // غیرمالی تشخیص داده شد (ذخیره متن نمی‌ماند)
    const val NEEDS_ACCOUNT = 2  // مالی ولی حساب ناشناخته
    const val NEEDS_TEMPLATE = 3 // حساب شناخته ولی استخراج ناموفق/مبهم
    const val DRAFT_READY = 4    // پیش‌نویس تراکنش ساخته شده، منتظر تأیید کاربر
    const val DONE = 5           // تراکنش تأیید/ثبت شد
    const val DISMISSED = 6      // کاربر صرف‌نظر کرد
}

/**
 * فرستنده‌ای که کاربر آن را «تبلیغاتی» علامت زده است.
 * پیامک‌های بعدی این فرستنده بدون مزاحمت و بدون ذخیره متن، کنار گذاشته می‌شوند.
 */
@Entity(
    tableName = "blocked_senders",
    indices = [Index(value = ["sender"], unique = true)]
)
data class BlockedSenderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val createdAt: Long
)

/** پیامک مالی یا مشکوک به مالی در صف بررسی. */
@Entity(
    tableName = "sms_candidates",
    indices = [Index(value = ["fingerprint"], unique = true), Index("status"), Index("receivedAt")]
)
data class SmsCandidateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    /** اثر انگشت پایدار برای جلوگیری از پردازش تکراری. */
    val fingerprint: String,
    val status: Int = SmsStatus.RAW,
    val matchedAccountId: Long? = null,
    val matchedTemplateId: Long? = null,
    /** نتیجه استخراج به شکل JSON. */
    val extractionJson: String? = null,
    val updatedAt: Long
)

/** قالب پیامک آموزش‌دیده. قواعد فیلد به شکل JSON از FieldRule ها. */
@Entity(
    tableName = "sms_templates",
    indices = [Index("sender"), Index("enabled")]
)
data class SmsTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sender: String,
    val bankName: String = "",
    val version: Int = 1,
    val enabled: Boolean = true,
    /** JSON آرایه‌ای از قواعد anchor-based. */
    val rulesJson: String,
    /** نمونه پیامکی که قالب از آن ساخته شد. */
    val sampleBody: String,
    /** واحد مبلغ در این قالب: RIAL یا TOMAN. */
    val amountUnit: String = "RIAL",
    val createdAt: Long
)

object TxDirection {
    const val DEPOSIT = 0   // واریز
    const val WITHDRAW = 1  // برداشت
}

object TxNature {
    const val UNKNOWN = 0
    const val INCOME = 1
    const val EXPENSE = 2
    const val TRANSFER = 3
}

object TxStatus {
    const val PENDING = 0    // نیازمند تأیید کاربر
    const val CONFIRMED = 1  // قطعی
}

object TxSource {
    const val MANUAL = 0
    const val SMS = 1
}

@Entity(
    tableName = "transactions",
    indices = [Index("accountId"), Index("occurredAt"), Index("status"), Index("categoryId"), Index("transferGroupId"), Index("smsId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    /** مبلغ به ریال، همیشه مثبت. */
    val amountRial: Long,
    val direction: Int,
    val nature: Int = TxNature.UNKNOWN,
    val categoryId: Long? = null,
    /** «برای چه بود؟» */
    val description: String = "",
    val occurredAt: Long,
    val recordedAt: Long,
    /** آیا زمان تراکنش از پیامک استخراج شده یا زمان دریافت است. */
    val timeIsApproximate: Boolean = false,
    val source: Int = TxSource.MANUAL,
    val smsId: Long? = null,
    val status: Int = TxStatus.PENDING,
    val transferGroupId: Long? = null,
    /** مانده اعلام‌شده در پیامک (ریال)، در صورت وجود. */
    val balanceAfterRial: Long? = null,
    val counterparty: String = "",
    val refNumber: String = "",
    /** آیا کاربر این تراکنش را دستی ویرایش کرده؟ پردازش خودکار مجدد نباید بازنویسی کند. */
    val userEdited: Boolean = false
)

/** گروه انتقال بین حساب‌های خود کاربر. */
@Entity(tableName = "transfer_groups")
data class TransferGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    /** true اگر فقط یک سمت انتقال ثبت شده باشد. */
    val incomplete: Boolean = true,
    val note: String = ""
)

object CategoryKind {
    const val EXPENSE = 0
    const val INCOME = 1
    const val BOTH = 2
}

@Entity(tableName = "categories", indices = [Index("archived")])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Long,
    val kind: Int = CategoryKind.EXPENSE,
    val archived: Boolean = false,
    val builtin: Boolean = false
)

/** قانون دسته‌بندی خودکار: اگر کلیدواژه در متن/طرف مقابل بود، دسته پیشنهاد شود. */
@Entity(tableName = "category_rules", indices = [Index("categoryId"), Index("enabled")])
data class CategoryRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val categoryId: Long,
    val enabled: Boolean = true,
    /** قوانین صریح کاربر اولویت بالاتری دارند. */
    val priority: Int = 0,
    val createdByUser: Boolean = true,
    val createdAt: Long
)

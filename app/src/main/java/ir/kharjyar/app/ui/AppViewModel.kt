package ir.kharjyar.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.core.balance.TxSummarizer
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.Repository
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.BlockedSenderEntity
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.SmsStatus
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.prefs.AppSettings
import ir.kharjyar.app.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel سراسری: تنظیمات، وضعیت قفل و جریان‌های داده مشترک.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val application = app as KharjYarApp
    val repo: Repository = application.repository
    val settingsRepo: SettingsRepository = application.settings

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val accounts: StateFlow<List<AccountEntity>> = repo.accountDao.observeAll()
    val debtPeople = repo.db.debtDao().observePeople().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val debts = repo.db.debtDao().observeDebts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val debtPayments = repo.db.debtDao().observePayments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val checks = repo.db.checkDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repo.categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** فرستنده‌هایی که کاربر تبلیغاتی علامت زده است. */
    val blockedSenders: StateFlow<List<BlockedSenderEntity>> = repo.blockedSenderDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repo.txDao.observeRecent(8)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repo.txDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * حساب پیش‌فرض انتخاب‌شده در تنظیمات (یا null یعنی «همه حساب‌ها»).
     * اگر حساب حذف/بایگانی شده باشد، به‌صورت خودکار null برگردانده می‌شود.
     */
    val defaultAccount: StateFlow<AccountEntity?> =
        combine(settingsRepo.settings, repo.accountDao.observeAll()) { s, list ->
            s.defaultAccountId?.let { id -> list.firstOrNull { it.id == id && !it.archived } }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** تراکنش‌های محدود به حساب پیش‌فرض (اگر انتخاب شده باشد). */
    val scopedTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, defaultAccount) { txs, acc ->
            if (acc == null) txs else txs.filter { it.accountId == acc.id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** تراکنش‌های اخیر محدود به حساب پیش‌فرض. */
    val scopedRecent: StateFlow<List<TransactionEntity>> =
        combine(recentTransactions, defaultAccount) { txs, acc ->
            if (acc == null) txs else txs.filter { it.accountId == acc.id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val reviewCount: StateFlow<Int> = repo.smsDao.observeCountByStatus(
        listOf(SmsStatus.RAW, SmsStatus.NEEDS_ACCOUNT, SmsStatus.NEEDS_TEMPLATE, SmsStatus.DRAFT_READY)
    ).stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val pendingTxCount: StateFlow<Int> = repo.txDao.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // ---------- خلاصه کارت اصلی ----------

    /** بازه کارت اصلی صفحه خانه. */
    enum class SummaryRange { MONTH, ALL }

    data class MonthSummary(
        val monthTitle: String = "",
        val incomeRial: Long = 0,
        val expenseRial: Long = 0,
        val pendingCount: Int = 0,
        val pendingIncomeRial: Long = 0,
        val pendingExpenseRial: Long = 0,
        val range: SummaryRange = SummaryRange.MONTH,
        /** آیا بیرون از این بازه داده‌ای هست؟ برای پیشنهاد «همه» بعد از بازیابی بکاپ. */
        val hasDataOutsideRange: Boolean = false
    )

    private val _summaryRange = MutableStateFlow(SummaryRange.MONTH)
    val summaryRange: StateFlow<SummaryRange> = _summaryRange

    fun toggleSummaryRange() {
        _summaryRange.value =
            if (_summaryRange.value == SummaryRange.MONTH) SummaryRange.ALL else SummaryRange.MONTH
    }

    /**
     * خلاصه کارت اصلی مستقیماً از همان فهرست تراکنش‌های حافظه ساخته می‌شود که
     * کارت‌ها و نمودار از آن استفاده می‌کنند. به همین دلیل با تعویض کارت یا با
     * ورود داده تازه (مثلاً بازیابی بکاپ) بدون هیچ تازه‌سازی دستی، درست و
     * فیلترشده به‌روز می‌شود.
     */
    val monthSummary: StateFlow<MonthSummary> =
        combine(allTransactions, defaultAccount, _summaryRange) { txs, acc, range ->
            val today = PersianDate.today()
            val (from, to) = repo.currentPersianMonthRange()
            val accountId = acc?.id
            val month = TxSummarizer.summarize(txs, accountId, from, to)
            val all = TxSummarizer.summarize(txs, accountId)
            val chosen = if (range == SummaryRange.MONTH) month else all
            MonthSummary(
                monthTitle = if (range == SummaryRange.MONTH)
                    "${today.monthName()} ${Digits.toPersian(today.year.toString())}"
                else "همه تراکنش‌ها",
                incomeRial = chosen.incomeRial,
                expenseRial = chosen.expenseRial,
                pendingCount = chosen.pendingCount,
                pendingIncomeRial = chosen.pendingIncomeRial,
                pendingExpenseRial = chosen.pendingExpenseRial,
                range = range,
                hasDataOutsideRange = !all.isEmpty && month.isEmpty
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MonthSummary())

    // ---------- قفل برنامه ----------
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked
    private var backgroundedAt: Long = 0

    fun onAppStart() {
        if (settings.value.appLockEnabled) _locked.value = true
    }

    fun onAppBackground() {
        backgroundedAt = System.currentTimeMillis()
    }

    fun onAppForeground() {
        val s = settings.value
        if (s.appLockEnabled && backgroundedAt > 0 &&
            System.currentTimeMillis() - backgroundedAt > s.lockTimeoutSeconds * 1000L
        ) {
            _locked.value = true
        }
    }

    fun unlock() { _locked.value = false }
}

package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import ir.kharjyar.app.ui.theme.LocalAppSkin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import kotlinx.coroutines.launch

/**
 * ثبت دستی تراکنش — بدون نیاز به هیچ مجوزی کار می‌کند.
 *
 * @param presetDirection اگر کاربر پیش از ورود، «واریز» یا «برداشت» را انتخاب کرده باشد،
 * فرم با همان حالت باز می‌شود و پرسش «این پول چه بود؟» اصلاً نمایش داده نمی‌شود.
 * @param presetTransfer وقتی از دکمه «انتقال وجه» آمده‌ایم؛ ماهیت تراکنش انتقال است و
 * فقط جهت آن (از این حساب رفت یا به این حساب آمد) پرسیده می‌شود.
 */
@Composable
fun ManualEntryScreen(
    viewModel: AppViewModel,
    nav: NavHostController,
    presetDirection: Int? = null,
    presetTransfer: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()

    var accountId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(presetDirection ?: TxDirection.WITHDRAW) }
    var nature by remember {
        mutableStateOf(
            when {
                presetTransfer -> TxNature.TRANSFER
                direction == TxDirection.DEPOSIT -> TxNature.INCOME
                else -> TxNature.EXPENSE
            }
        )
    }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    /** انتقال به یکی از حساب‌های ثبت‌شده خود کاربر یا به شخص دیگر. */
    var transferToOwn by remember { mutableStateOf(true) }
    var targetAccountId by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    // تاریخ و ساعتِ همین لحظهٔ باز شدن فرم؛ کاربر می‌تواند تغییرش دهد
    val now = remember { PersianDate.nowHourMinute() }
    var date by remember { mutableStateOf(PersianDate.today()) }
    var hour by remember { mutableStateOf(now.first) }
    var minute by remember { mutableStateOf(now.second) }
    var error by remember { mutableStateOf<String?>(null) }

    val active = accounts.filter { !it.archived }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // سربرگ نوع تراکنش وقتی از دیالوگ «واریز یا برداشت» آمده‌ایم
        if (presetDirection != null) {
            DirectionHeader(direction, presetTransfer)
        }

        // میان‌بر به ثبت با جمله فارسی
        OutlinedButton(
            onClick = { nav.navigate("quickAdd") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("ثبت سریع")
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        if (active.isEmpty()) {
            Text("ابتدا یک حساب معرفی کنید.", color = MaterialTheme.colorScheme.error)
            Button(onClick = { nav.navigate("accountEdit/0") }) { Text("افزودن حساب") }
        } else {
            AccountPicker(active, accountId) { accountId = it }

            AmountTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "مبلغ (${if (settings.moneyUnit == MoneyUnit.TOMAN) "تومان" else "ریال"})",
                supportingText = Money.inputToRial(amountText, settings.moneyUnit)
                    ?.let { Money.format(it, settings.moneyUnit) },
                modifier = Modifier.fillMaxWidth()
            )

            if (presetTransfer) {
                // انتقال دسته‌بندی ندارد؛ فقط مشخص می‌کنیم مقصد یکی از حساب‌های
                // خود کاربر است یا حساب شخص دیگر.
                ComboBox(
                    label = "مقصد انتقال",
                    options = listOf(true, false),
                    selected = transferToOwn,
                    labelOf = { if (it) "حساب دیگر خودم در خرج‌یار" else "حساب شخص دیگر" },
                    onSelect = { transferToOwn = it; targetAccountId = null }
                )
                if (transferToOwn) {
                    val targets = active.filter { it.id != accountId }
                    ComboBox(
                        label = "واریز به حساب",
                        options = targets.map { it.id },
                        selected = targetAccountId ?: 0L,
                        labelOf = { id -> targets.firstOrNull { it.id == id }?.let { "${it.title} — ${it.bankName}" } ?: "انتخاب حساب مقصد" },
                        onSelect = { targetAccountId = it }
                    )
                    Text(
                        "برداشت از حساب مبدأ و واریز به حساب مقصد هم‌زمان ثبت می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (presetDirection == null) {
                    NaturePicker(
                        nature = nature,
                        direction = direction,
                        onNature = { nature = it },
                        onDirection = { direction = it }
                    )
                }
                CategoryPicker(
                    categories = categories,
                    selectedId = categoryId,
                    nature = nature,
                    onCreate = { name ->
                        scope.launch {
                            val id = viewModel.repo.categoryDao.insert(
                                ir.kharjyar.app.data.db.CategoryEntity(name = name, colorArgb = 0xFF6C8AE4)
                            )
                            categoryId = id
                        }
                    }
                ) { categoryId = it }
            }
            DatePickerRow(date, hour, minute, onDate = { date = it }, onTime = { h, m -> hour = h; minute = m })

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text(
                        when {
                            nature == TxNature.TRANSFER -> "انتقال بابت چه بود؟"
                            direction == TxDirection.DEPOSIT -> "واریز بابت چه بود؟"
                            else -> "خرید بابت چه بود؟"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().keepAboveKeyboard()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val acc = accountId
                    val amount = Money.inputToRial(amountText, settings.moneyUnit)
                    when {
                        acc == null -> error = "حساب را انتخاب کنید"
                        amount == null || amount <= 0 -> error = "مبلغ معتبر وارد کنید"
                        presetTransfer && transferToOwn && targetAccountId == null -> error = "حساب مقصد را انتخاب کنید"
                        presetTransfer && transferToOwn && targetAccountId == acc -> error = "حساب مبدأ و مقصد نمی‌تواند یکی باشد"
                        else -> {
                            error = null
                            scope.launch {
                                val occurredAt = PersianDate.toMillis(date, hour, minute)
                                if (presetTransfer && transferToOwn) {
                                    viewModel.repo.addInternalTransfer(
                                        fromAccountId = acc,
                                        toAccountId = requireNotNull(targetAccountId),
                                        amountRial = amount,
                                        description = description.trim(),
                                        occurredAt = occurredAt
                                    )
                                } else {
                                    viewModel.repo.addManualTransaction(
                                        accountId = acc,
                                        amountRial = amount,
                                        direction = if (presetTransfer) TxDirection.WITHDRAW else direction,
                                        nature = nature,
                                        categoryId = if (presetTransfer) null else categoryId,
                                        description = description.trim(),
                                        occurredAt = occurredAt,
                                        counterparty = if (presetTransfer) "حساب شخص دیگر" else ""
                                    )
                                }
                                ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                                nav.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ثبت") }
        }
    }
}

/** نوار کوچک بالای فرم که نشان می‌دهد چه چیزی در حال ثبت است. */
@Composable
private fun DirectionHeader(direction: Int, transfer: Boolean = false) {
    val skin = LocalAppSkin.current
    val deposit = direction == TxDirection.DEPOSIT
    val tint = when {
        transfer -> skin.accent
        deposit -> skin.incomeColor
        else -> skin.expenseColor
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.14f))
            .border(1.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            when {
                transfer -> Icons.Filled.SwapHoriz
                deposit -> Icons.AutoMirrored.Filled.TrendingUp
                else -> Icons.AutoMirrored.Filled.TrendingDown
            },
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            when {
                transfer -> "ثبت مبلغی که بین حساب‌ها جابه‌جا شده است"
                deposit -> "ثبت مبلغی که به حساب واریز شده است"
                else -> "ثبت مبلغی که از حساب برداشت شده است"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = skin.onBackdrop
        )
    }
}

package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.money.Money
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountSenderEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.core.card.CardScan
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.CardScannerDialog
import ir.kharjyar.app.ui.components.ColorPicker
import ir.kharjyar.app.ui.components.BankCard
import ir.kharjyar.app.ui.components.BankLogo
import ir.kharjyar.app.ui.components.bankCardColorArgb
import ir.kharjyar.app.ui.components.SearchableComboBox
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.NumberTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

/** رنگ پیش‌فرض حساب جدید. */
private const val DEFAULT_ACCOUNT_COLOR = 0xFF3F51B5

/**
 * فهرست بانک‌ها و مؤسسه‌های اعتباری دارای خدمات بانکی (عضو شتاب).
 * «توسعه تعاون» طبق درخواست کاربر اولین گزینه است. بانک‌های ادغام‌شده (انصار،
 * قوامین، حکمت ایرانیان، مهر اقتصاد) هم مانده‌اند چون هنوز کارت و حساب قدیمی
 * با نام آن‌ها وجود دارد.
 */
private val bankNames = listOf(
    "توسعه تعاون",
    "ملی", "ملت", "صادرات", "تجارت", "سپه", "کشاورزی", "مسکن", "رفاه", "پست بانک",
    "توسعه صادرات", "صنعت و معدن", "پاسارگاد", "پارسیان", "سامان", "اقتصاد نوین",
    "سرمایه", "کارآفرین", "سینا", "شهر", "دی", "آینده", "گردشگری", "ایران زمین",
    "خاورمیانه", "رسالت", "قرض‌الحسنه مهر", "ایران ونزوئلا", "بلو",
    "مؤسسه اعتباری ملل", "مؤسسه اعتباری نور",
    "انصار", "قوامین", "حکمت ایرانیان", "مهر اقتصاد",
    "سایر"
)

/** معرفی/ویرایش حساب. id == 0 یعنی حساب جدید. */
@Composable
fun AccountEditScreen(
    viewModel: AppViewModel,
    nav: NavHostController,
    accountId: Long,
    prefillSender: String = "",
    onSaved: ((Long) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()

    var title by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(DEFAULT_ACCOUNT_COLOR) }
    var maskedNumber by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv2 by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }
    var archived by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf<AccountEntity?>(null) }
    val senders = remember { mutableStateListOf<Pair<String, String>>() } // sender to identifierHint
    val senderIds = remember { mutableStateListOf<Long>() }
    var newSender by remember { mutableStateOf(prefillSender) }
    var newHint by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }
    var txCount by remember { mutableStateOf(0) }

    LaunchedEffect(accountId) {
        if (accountId > 0) {
            viewModel.repo.accountDao.byId(accountId)?.let { a ->
                existing = a
                title = a.title; bankName = a.bankName; color = a.colorArgb
                maskedNumber = a.maskedNumber; archived = a.archived
                accountNumber = a.accountNumber; iban = a.iban
                cardNumber = a.cardNumber; cardExpiry = a.cardExpiry; cardCvv2 = a.cardCvv2
                initialBalance = a.initialBalanceRial?.toString() ?: ""
            }
            viewModel.repo.accountDao.sendersOf(accountId).forEach {
                senders.add(it.sender to it.identifierHint)
                senderIds.add(it.id)
            }
            txCount = viewModel.repo.accountDao.transactionCount(accountId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        // ---------- پیش‌نمایش زنده کارت ----------
        BankCard(
            account = AccountEntity(
                id = accountId,
                title = title.ifBlank { "عنوان حساب" },
                bankName = bankName,
                colorArgb = color,
                accountNumber = accountNumber,
                iban = iban,
                cardNumber = cardNumber,
                cardExpiry = cardExpiry,
                cardCvv2 = cardCvv2,
                createdAt = 0L
            ),
            balanceText = null,
            balanceCaption = null,
            selected = true,
            masked = false,
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- پایه ----------
        FormSection("اطلاعات پایه") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان دلخواه (مثل «حساب حقوق»)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            SearchableComboBox(
                label = "نام بانک",
                options = bankNames,
                value = bankName,
                // رنگ کارت خودش از روی لوگوی همان بانک انتخاب می‌شود
                onValueChange = { bankName = it; color = bankCardColorArgb(it, color) },
                placeholder = "حروف اول نام بانک را بنویسید",
                // نشان هر بانک کنار نامش، هم در فیلد و هم در فهرست انتخاب
                leadingOf = { BankLogo(bankName = it, size = 26.dp) }
            )
        }

        // ---------- اسکن کارت و شماره‌ها ----------
        FormSection("اسکن کارت") {
            Text(
                "هر کدام را خالی بگذارید، روی کارت نمایش داده نمی‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // خواندن شماره کارت/شبا/انقضا/CVV2 از روی خود کارت؛ همه چیز روی گوشی
            OutlinedButton(
                onClick = { scanMessage = null; showScanner = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("اسکن کارت با دوربین")
            }
            scanMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            NumberTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it.filter(Char::isDigit).take(16) },
                label = "شماره کارت (۱۶ رقم)",
                maxDigits = 16,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { raw ->
                        // فقط رقم نگه می‌داریم و ممیز را خودکار بعد از ماه می‌گذاریم
                        val d = Digits.normalize(raw).filter(Char::isDigit).take(6)
                        cardExpiry = when {
                            d.length <= 2 -> d
                            else -> d.substring(0, 2) + "/" + d.substring(2)
                        }
                    },
                    label = { Text("انقضا (ماه/سال ۱۴۰۵)") },
                    placeholder = { Text("۰۶/۱۴۰۸") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                NumberTextField(
                    value = cardCvv2,
                    onValueChange = { cardCvv2 = it.filter(Char::isDigit).take(4) },
                    label = "CVV2",
                    maxDigits = 4,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { raw ->
                    // شماره حساب بعضی بانک‌ها نقطه یا خط تیره دارد (مثل ۱۲۳۴.۵۶.۷۸۹)
                    accountNumber = Digits.normalize(raw)
                        .filter { it.isDigit() || it == '.' || it == '-' }
                        .take(30)
                },
                label = { Text("شماره حساب") },
                placeholder = { Text("مثلاً ۱۲۳۴.۵۶.۷۸۹۰۱۲۳.۱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            NumberTextField(
                value = iban,
                onValueChange = { iban = it.filter(Char::isDigit).take(24) },
                label = "شبا (۲۴ رقم، بدون IR)",
                maxDigits = 24,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = maskedNumber,
                onValueChange = { maskedNumber = it },
                label = { Text("شناسه کوتاه برای تطبیق پیامک (مثل ****۱۲۳۴)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        AmountTextField(
            value = initialBalance,
            onValueChange = { initialBalance = it },
            label = "موجودی اولیه اختیاری (ریال)",
            supportingText = Digits.parseAmount(initialBalance)?.let { Money.format(it, settings.moneyUnit) },
            modifier = Modifier.fillMaxWidth()
        )

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("سرشماره‌های پیامک این حساب", style = MaterialTheme.typography.titleSmall)
                Text(
                    "یک سرشماره می‌تواند برای چند حساب باشد؛ «شناسه داخل متن» (مثل ۴ رقم آخر کارت) حساب درست را مشخص می‌کند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                senders.forEachIndexed { idx, (s, h) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            Digits.toPersian(s) + if (h.isNotBlank()) " (شناسه: ${Digits.toPersian(h)})" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = {
                            senders.removeAt(idx)
                            if (idx < senderIds.size) {
                                val id = senderIds.removeAt(idx)
                                scope.launch { viewModel.repo.accountDao.deleteSender(id) }
                            }
                        }) { Icon(Icons.Filled.Delete, contentDescription = "حذف سرشماره") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newSender, onValueChange = { newSender = it }, label = { Text("سرشماره") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = newHint, onValueChange = { newHint = it }, label = { Text("شناسه داخل متن") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedButton(onClick = {
                    if (newSender.isNotBlank()) {
                        senders.add(newSender.trim() to newHint.trim())
                        senderIds.add(0L)
                        newSender = ""; newHint = ""
                    }
                }) { Text("افزودن سرشماره") }
            }
        }

        if (accountId > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("بایگانی حساب", style = MaterialTheme.typography.bodyLarge)
                androidx.compose.material3.Switch(checked = archived, onCheckedChange = { archived = it })
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                if (title.isBlank()) { error = "عنوان حساب را وارد کنید"; return@Button }
                error = null
                scope.launch {
                    val balanceRial = Digits.parseAmount(initialBalance)
                    val now = System.currentTimeMillis()
                    val id: Long
                    if (existing == null) {
                        id = viewModel.repo.accountDao.insert(
                            AccountEntity(
                                title = title.trim(), bankName = bankName.trim(), colorArgb = color,
                                maskedNumber = maskedNumber.trim(),
                                accountNumber = accountNumber.trim(), iban = iban.trim(),
                                cardNumber = cardNumber.trim(), cardExpiry = cardExpiry.trim(),
                                cardCvv2 = cardCvv2.trim(),
                                initialBalanceRial = balanceRial,
                                initialBalanceAt = if (balanceRial != null) now else null,
                                archived = false, createdAt = now
                            )
                        )
                    } else {
                        id = existing!!.id
                        viewModel.repo.accountDao.update(
                            existing!!.copy(
                                title = title.trim(), bankName = bankName.trim(), colorArgb = color,
                                maskedNumber = maskedNumber.trim(),
                                accountNumber = accountNumber.trim(), iban = iban.trim(),
                                cardNumber = cardNumber.trim(), cardExpiry = cardExpiry.trim(),
                                cardCvv2 = cardCvv2.trim(),
                                initialBalanceRial = balanceRial,
                                initialBalanceAt = if (balanceRial != null) (existing!!.initialBalanceAt ?: now) else null,
                                archived = archived
                            )
                        )
                    }
                    // سرشماره‌های جدید
                    senders.forEachIndexed { idx, (s, h) ->
                        if (idx >= senderIds.size || senderIds[idx] == 0L) {
                            viewModel.repo.accountDao.insertSender(
                                AccountSenderEntity(accountId = id, sender = s, identifierHint = h)
                            )
                        }
                    }
                    if (onSaved != null) onSaved(id) else nav.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ذخیره حساب") }

        if (accountId > 0) {
            TextButton(onClick = { showDelete = true }) {
                Text("حذف حساب", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showScanner) {
        CardScannerDialog(
            onDismiss = { showScanner = false },
            onResult = { result: CardScan ->
                showScanner = false
                val filled = mutableListOf<String>()
                if (result.cardNumber.isNotBlank()) { cardNumber = result.cardNumber; filled += "شماره کارت" }
                if (result.expiry.isNotBlank()) { cardExpiry = result.expiry; filled += "تاریخ انقضا" }
                if (result.cvv2.isNotBlank()) { cardCvv2 = result.cvv2; filled += "CVV2" }
                if (result.iban.isNotBlank()) { iban = result.iban; filled += "شبا" }
                if (result.accountNumber.isNotBlank()) { accountNumber = result.accountNumber; filled += "شماره حساب" }
                if (result.bankName.isNotBlank() && bankName.isBlank()) {
                    bankName = result.bankName; filled += "نام بانک"
                    // رنگ کارت هم با لوگوی همان بانک هماهنگ می‌شود
                    color = bankCardColorArgb(bankName, color)
                }
                // شناسه کوتاه تطبیق پیامک را هم از چهار رقم آخر کارت پر می‌کنیم
                if (maskedNumber.isBlank() && cardNumber.length == 16) {
                    maskedNumber = "****" + cardNumber.takeLast(4)
                }
                scanMessage = if (filled.isEmpty()) {
                    "چیزی از کارت خوانده نشد. نور را بیشتر کنید و دوباره امتحان کنید یا دستی وارد کنید."
                } else {
                    val tail = if (result.accountNumber.isBlank())
                        " و شماره حساب را اگر روی کارت نبود دستی وارد کنید."
                    else "."
                    "خوانده شد: " + filled.joinToString("، ") + ". " +
                        "عنوان حساب را خودتان انتخاب کنید" + tail
                }
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("حذف حساب") },
            text = {
                Text(
                    if (txCount > 0)
                        "این حساب ${Digits.toPersian(txCount.toString())} تراکنش دارد. حذف آن پیشنهاد نمی‌شود؛ بایگانی گزینه امن‌تری است. مطمئنید؟"
                    else "حساب حذف شود؟"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.repo.accountDao.delete(accountId)
                        showDelete = false
                        nav.popBackStack()
                    }
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        existing?.let { viewModel.repo.accountDao.update(it.copy(archived = true)) }
                        showDelete = false
                        nav.popBackStack()
                    }
                }) { Text("بایگانی (پیشنهادی)") }
            }
        )
    }
}

/** یک بخش از فرم داخل کارت، با عنوان و فاصله‌گذاری یکنواخت. */
@Composable
private fun FormSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    SkinCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

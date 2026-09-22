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
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountSenderEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import kotlinx.coroutines.launch

private val accountColors = listOf(
    0xFF3F51B5, 0xFF00897B, 0xFFD81B60, 0xFFF4511E, 0xFF6D4C41,
    0xFF546E7A, 0xFF7B1FA2, 0xFF2E7D32, 0xFFC62828, 0xFF0277BD
)

private val bankNames = listOf(
    "ملی", "ملت", "صادرات", "تجارت", "سپه", "کشاورزی", "مسکن", "رفاه", "پاسارگاد",
    "پارسیان", "سامان", "اقتصاد نوین", "شهر", "دی", "سینا", "کارآفرین", "آینده",
    "گردشگری", "ایران زمین", "خاورمیانه", "رسالت", "قرض‌الحسنه مهر", "پست بانک", "سایر"
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
    var color by remember { mutableStateOf(accountColors[0]) }
    var maskedNumber by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }
    var archived by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf<AccountEntity?>(null) }
    val senders = remember { mutableStateListOf<Pair<String, String>>() } // sender to identifierHint
    val senderIds = remember { mutableStateListOf<Long>() }
    var newSender by remember { mutableStateOf(prefillSender) }
    var newHint by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var txCount by remember { mutableStateOf(0) }

    LaunchedEffect(accountId) {
        if (accountId > 0) {
            viewModel.repo.accountDao.byId(accountId)?.let { a ->
                existing = a
                title = a.title; bankName = a.bankName; color = a.colorArgb
                maskedNumber = a.maskedNumber; archived = a.archived
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
        
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان دلخواه (مثل «حساب حقوق»)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Text("نام بانک", style = MaterialTheme.typography.labelLarge)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bankNames.size) { i ->
                androidx.compose.material3.FilterChip(
                    selected = bankName == bankNames[i],
                    onClick = { bankName = bankNames[i] },
                    label = { Text(bankNames[i]) }
                )
            }
        }
        OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("یا نام بانک را بنویسید") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Text("رنگ", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            accountColors.take(10).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(c), CircleShape)
                        .border(
                            width = if (color == c) 3.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                        .clickable { color = c }
                )
            }
        }

        OutlinedTextField(
            value = maskedNumber,
            onValueChange = { maskedNumber = it },
            label = { Text("شناسه کارت/حساب (ترجیحاً ماسک‌شده مثل ****۱۲۳۴)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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

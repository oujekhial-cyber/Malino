package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ir.kharjyar.app.data.db.AccountSenderEntity
import ir.kharjyar.app.data.db.SmsCandidateEntity
import ir.kharjyar.app.data.db.SmsStatus
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

/**
 * معرفی حساب از روی پیامک ناشناخته، یا اتصال پیامک به حساب موجود.
 * پس از معرفی، «همان پیامک اولیه» دوباره پردازش می‌شود — از پیامک دوم شروع نمی‌کنیم.
 */
@Composable
fun AccountFromSmsScreen(viewModel: AppViewModel, nav: NavHostController, smsId: Long) {
    var showAdConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accounts by viewModel.accounts.collectAsState()
    var sms by remember { mutableStateOf<SmsCandidateEntity?>(null) }
    var mode by remember { mutableStateOf("choose") } // choose | new
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(smsId) {
        sms = viewModel.repo.smsDao.byId(smsId)
        loaded = true
    }

    if (!loaded) return
    val s = sms
    if (s == null) {
        Column(Modifier.fillMaxSize().padding(32.dp)) { Text("پیامک پیدا نشد") }
        return
    }

    suspend fun linkAndReprocess(accountId: Long) {
        // ثبت نگاشت فرستنده -> حساب (بدون hint؛ کاربر می‌تواند بعداً دقیق کند)
        val existing = viewModel.repo.accountDao.sendersOf(accountId)
        if (existing.none { it.sender == s.sender }) {
            viewModel.repo.accountDao.insertSender(
                AccountSenderEntity(accountId = accountId, sender = s.sender, identifierHint = "")
            )
        }
        // پردازش دوباره همان پیامک اولیه
        val fresh = viewModel.repo.smsDao.byId(s.id)
        if (fresh != null) {
            when (val outcome = viewModel.repo.processWithAccount(fresh, accountId)) {
                is ir.kharjyar.app.data.Repository.ProcessOutcome.DraftReady -> {
                    nav.navigate("tx/${outcome.txId}") { popUpTo("home") }
                    return
                }
                is ir.kharjyar.app.data.Repository.ProcessOutcome.NeedsTemplate -> {
                    nav.navigate("template/${s.id}") { popUpTo("home") }
                    return
                }
                else -> {}
            }
        }
        nav.popBackStack()
    }

    if (mode == "new") {
        AccountEditScreen(
            viewModel = viewModel,
            nav = nav,
            accountId = 0L,
            prefillSender = s.sender,
            onSaved = { newId -> scope.launch { linkAndReprocess(newId) } }
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("فرستنده: ${s.sender}", style = MaterialTheme.typography.titleSmall)
                Text(
                    PersianDate.formatDateTime(s.receivedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(s.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Text(
            "این پیامک مالی به نظر می‌رسد ولی حساب آن شناخته نشد. حساب جدید بسازید یا آن را به حساب موجود متصل کنید. پس از انتخاب، همین پیامک دوباره پردازش می‌شود.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = { mode = "new" }, modifier = Modifier.fillMaxWidth()) { Text("ساخت حساب جدید") }

        if (accounts.any { !it.archived }) {
            Text("یا اتصال به حساب موجود:", style = MaterialTheme.typography.titleSmall)
            accounts.filter { !it.archived }.forEach { a ->
                OutlinedButton(
                    onClick = { scope.launch { linkAndReprocess(a.id) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("${a.title} (${a.bankName})") }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // اگر این پیامک اصلاً مالی نیست، کاربر می‌تواند آن را کنار بگذارد
        Text(
            "اگر این پیامک مالی نیست:",
            style = MaterialTheme.typography.titleSmall
        )
        OutlinedButton(
            onClick = {
                scope.launch {
                    sms?.let {
                        viewModel.repo.smsDao.update(
                            it.copy(status = SmsStatus.DISMISSED, updatedAt = System.currentTimeMillis())
                        )
                    }
                    nav.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("صرف‌نظر از این پیامک") }

        TextButton(
            onClick = { showAdConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("این فرستنده تبلیغاتی است") }
    }

    // ---------- تأیید علامت‌گذاری فرستنده تبلیغاتی ----------
    if (showAdConfirm) {
        val sender = sms?.sender.orEmpty()
        AlertDialog(
            onDismissRequest = { showAdConfirm = false },
            title = { Text("پیامک تبلیغاتی") },
            text = {
                Text(
                    "پیام‌های «$sender» از این پس تبلیغاتی در نظر گرفته می‌شوند و " +
                        "بدون ذخیره متن نادیده گرفته می‌شوند.\n\n" +
                        "هر وقت خواستید می‌توانید از تنظیمات ← فرستنده‌های تبلیغاتی آن را برگردانید."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.repo.blockSender(sender)
                        showAdConfirm = false
                        nav.popBackStack()
                    }
                }) { Text("بله، تبلیغاتی است") }
            },
            dismissButton = {
                TextButton(onClick = { showAdConfirm = false }) { Text("انصراف") }
            }
        )
    }
}

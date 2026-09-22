package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.data.db.SmsStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.EmptyState
import kotlinx.coroutines.launch

/** صف بررسی: پیامک‌های نیازمند معرفی حساب، آموزش قالب یا تأیید پیش‌نویس. */
@Composable
fun ReviewScreen(viewModel: AppViewModel, nav: NavHostController) {
    // فرستنده‌ای که کاربر می‌خواهد تبلیغاتی علامت بزند (برای تأیید)
    var adSender by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val queue by remember {
        viewModel.repo.smsDao.observeByStatus(
            listOf(SmsStatus.RAW, SmsStatus.NEEDS_ACCOUNT, SmsStatus.NEEDS_TEMPLATE, SmsStatus.DRAFT_READY)
        )
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
            "هیچ موردی بدون تأیید شما ثبت قطعی نمی‌شود.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (queue.isEmpty()) {
            EmptyState("صف بررسی خالی است", "پیامک‌های مالی جدید که به کمک شما نیاز داشته باشند اینجا می‌آیند")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(queue.size) { i ->
                    val sms = queue[i]
                    val (label, route) = when (sms.status) {
                        SmsStatus.NEEDS_ACCOUNT -> "حساب ناشناخته — معرفی حساب" to "accountFromSms/${sms.id}"
                        SmsStatus.NEEDS_TEMPLATE -> "قالب ناشناخته — آموزش قالب" to "template/${sms.id}"
                        SmsStatus.DRAFT_READY -> "پیش‌نویس آماده — تکمیل و تأیید" to null
                        else -> "در انتظار پردازش" to null
                    }
                    SkinCard(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (route != null) {
                                nav.navigate(route)
                            } else if (sms.status == SmsStatus.DRAFT_READY) {
                                scope.launch {
                                    val tx = viewModel.repo.txDao.bySmsId(sms.id)
                                    if (tx != null) nav.navigate("tx/${tx.id}")
                                }
                            } else {
                                // RAW: پردازش دوباره
                                scope.launch { viewModel.repo.processSms(sms.id) }
                            }
                        }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(sms.sender, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    PersianDate.formatDateTime(sms.receivedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                sms.body.take(120) + if (sms.body.length > 120) "…" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                AssistChip(onClick = {}, label = { Text(label) })
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    // علامت‌گذاری فرستنده به‌عنوان تبلیغاتی:
                                    // پیام‌های بعدی همین فرستنده خودکار نادیده گرفته می‌شوند
                                    TextButton(onClick = { adSender = sms.sender }) {
                                        Text("تبلیغاتی است")
                                    }
                                    TextButton(onClick = {
                                        scope.launch {
                                            viewModel.repo.smsDao.update(
                                                sms.copy(status = SmsStatus.DISMISSED, updatedAt = System.currentTimeMillis())
                                            )
                                        }
                                    }) { Text("صرف‌نظر") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- تأیید علامت‌گذاری فرستنده تبلیغاتی ----------
    adSender?.let { sender ->
        AlertDialog(
            onDismissRequest = { adSender = null },
            title = { Text("پیامک تبلیغاتی") },
            text = {
                Text(
                    "پیام‌های «$sender» از این پس تبلیغاتی در نظر گرفته می‌شوند و " +
                        "بدون ذخیره متن، نادیده گرفته می‌شوند.\n\n" +
                        "هر وقت خواستید می‌توانید از تنظیمات ← فرستنده‌های تبلیغاتی آن را برگردانید."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.repo.blockSender(sender) }
                    adSender = null
                }) { Text("بله، تبلیغاتی است") }
            },
            dismissButton = { TextButton(onClick = { adSender = null }) { Text("انصراف") } }
        )
    }
}

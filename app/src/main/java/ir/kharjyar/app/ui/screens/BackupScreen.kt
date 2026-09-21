package ir.kharjyar.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.backup.BackupCrypto
import ir.kharjyar.app.core.backup.BackupManager
import ir.kharjyar.app.core.backup.BackupPayload
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** بکاپ و بازیابی محلی رمزنگاری‌شده با انتخاب محل فایل توسط کاربر (SAF). */
@Composable
fun BackupScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { BackupManager(viewModel.repo, viewModel.settingsRepo) }

    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<BackupPayload?>(null) }
    var busy by remember { mutableStateOf(false) }

    val saveBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && password.isNotBlank()) {
            busy = true
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.Default) { manager.createBackup(password.toCharArray()) }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    }
                    message = "بکاپ با موفقیت ذخیره شد."; isError = false
                    password = ""; passwordRepeat = ""
                } catch (e: Exception) {
                    message = "خطا در ساخت بکاپ"; isError = true
                } finally { busy = false }
            }
        }
    }

    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && restorePassword.isNotBlank()) {
            busy = true
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } ?: throw Exception("read failed")
                    val payload = withContext(Dispatchers.Default) {
                        manager.validate(bytes, restorePassword.toCharArray())
                    }
                    pendingRestore = payload
                    message = null
                } catch (e: BackupCrypto.WrongPasswordOrCorruptException) {
                    message = "رمز اشتباه است یا فایل آسیب دیده. داده فعلی تغییری نکرد."; isError = true
                } catch (e: BackupCrypto.BackupFormatException) {
                    message = e.message ?: "فایل نامعتبر"; isError = true
                } catch (e: Exception) {
                    message = "خواندن فایل ممکن نشد. داده فعلی تغییری نکرد."; isError = true
                } finally { busy = false }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("بکاپ و بازیابی", style = MaterialTheme.typography.headlineSmall)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Text(
                "بکاپ یک فایل واحد رمزنگاری‌شده (AES-256-GCM) است. رمز عبور آن مستقل از رمز گوشی و اثر انگشت است و باید جداگانه انتخاب شود.\n\n" +
                    "مهم: اگر رمز بکاپ را فراموش کنید، بازیابی به هیچ روشی ممکن نیست.",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Text("ساخت بکاپ", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("رمز عبور بکاپ (حداقل ۶ کاراکتر)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = passwordRepeat, onValueChange = { passwordRepeat = it },
            label = { Text("تکرار رمز عبور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Button(
            enabled = !busy,
            onClick = {
                when {
                    password.length < 6 -> { message = "رمز حداقل ۶ کاراکتر باشد"; isError = true }
                    password != passwordRepeat -> { message = "تکرار رمز یکسان نیست"; isError = true }
                    else -> {
                        message = null
                        val date = ir.kharjyar.app.core.date.PersianDate.today().format(persianDigits = false).replace("/", "-")
                        saveBackup.launch("kharjyar-backup-$date.khbk")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("انتخاب محل و ساخت بکاپ") }

        Text("بازیابی", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = restorePassword, onValueChange = { restorePassword = it },
            label = { Text("رمز عبور فایل بکاپ") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedButton(
            enabled = !busy,
            onClick = {
                if (restorePassword.isBlank()) { message = "رمز فایل را وارد کنید"; isError = true }
                else openBackup.launch(arrayOf("*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("انتخاب فایل بکاپ") }

        message?.let {
            Text(it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }

    pendingRestore?.let { payload ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("تأیید بازیابی") },
            text = {
                Text(
                    "فایل معتبر است.\n" +
                        "حساب‌ها: ${Digits.toPersian(payload.accounts.size.toString())}\n" +
                        "تراکنش‌ها: ${Digits.toPersian(payload.transactions.size.toString())}\n" +
                        "دسته‌ها: ${Digits.toPersian(payload.categories.size.toString())}\n" +
                        "قالب‌ها: ${Digits.toPersian(payload.templates.size.toString())}\n\n" +
                        "با ادامه، همه داده‌های فعلی با محتوای بکاپ جایگزین می‌شوند."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        busy = true
                        try {
                            manager.restore(payload)
                            message = "بازیابی با موفقیت انجام شد."; isError = false
                        } catch (e: Exception) {
                            message = "بازیابی ناموفق بود"; isError = true
                        } finally {
                            busy = false
                            pendingRestore = null
                            restorePassword = ""
                        }
                    }
                }) { Text("جایگزینی کامل") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("انصراف") } }
        )
    }
}

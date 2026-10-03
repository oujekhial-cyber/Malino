package ir.kharjyar.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.backup.BackupCrypto
import ir.kharjyar.app.core.backup.BackupManager
import ir.kharjyar.app.core.backup.BackupPayload
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * بکاپ و بازیابی محلی با انتخاب محل فایل توسط کاربر (SAF).
 * رمزگذاری اختیاری است؛ در بازیابی ابتدا فایل انتخاب می‌شود و فقط اگر رمزدار بود رمز پرسیده می‌شود.
 */
@Composable
fun BackupScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { BackupManager(viewModel.repo, viewModel.settingsRepo) }
    val skin = LocalAppSkin.current

    // ---------- ساخت بکاپ ----------
    var encrypt by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    // ---------- بازیابی ----------
    /** بایت‌های فایل انتخاب‌شده که منتظر رمز است. */
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    var restorePassword by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<BackupPayload?>(null) }
    /** بازیابی جایگزینی که منتظر ذخیره بکاپ ایمنی از اطلاعات فعلی است. */
    var safetyBackupPayload by remember { mutableStateOf<BackupPayload?>(null) }

    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    // پیام نتیجه هر عملیات فقط چهار ثانیه نمایش داده می‌شود.
    LaunchedEffect(message) {
        val shown = message ?: return@LaunchedEffect
        delay(4_000)
        if (message == shown) message = null
    }

    val saveBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                try {
                    val pwd = if (encrypt) password.toCharArray() else null
                    val bytes = withContext(Dispatchers.Default) { manager.createBackup(pwd) }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    }
                    message = if (encrypt) "بکاپ رمزدار ذخیره شد." else "بکاپ بدون رمز ذخیره شد."
                    isError = false
                    password = ""; passwordRepeat = ""
                } catch (e: Exception) {
                    message = "خطا در ساخت بکاپ"; isError = true
                } finally { busy = false }
            }
        }
    }

    /** ابتدا نسخه ایمنی داده فعلی ذخیره می‌شود؛ فقط بعد از موفقیت، جایگزینی اجرا می‌شود. */
    val saveSafetyBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val payload = safetyBackupPayload
        if (uri == null || payload == null) {
            safetyBackupPayload = null
            message = "جایگزینی لغو شد و اطلاعات فعلی تغییر نکرد."
        } else scope.launch {
            busy = true
            try {
                val bytes = withContext(Dispatchers.Default) { manager.createBackup(null) }
                withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openOutputStream(uri)).use { it.write(bytes) }
                }
                manager.restore(payload)
                message = "از اطلاعات قبلی بکاپ گرفته شد و بازیابی جایگزین با موفقیت انجام شد."
                isError = false
                pendingRestore = null
            } catch (_: Exception) {
                message = "بکاپ ایمنی یا بازیابی کامل نشد؛ عملیات متوقف شد."
                isError = true
            } finally {
                busy = false
                safetyBackupPayload = null
            }
        }
    }

    /** مرحله ۱ بازیابی: فقط فایل انتخاب می‌شود؛ رمز بعداً و فقط در صورت نیاز. */
    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } ?: throw Exception("read failed")

                    if (manager.needsPassword(bytes)) {
                        // فایل رمزدار است: حالا از کاربر رمز می‌پرسیم
                        pendingBytes = bytes
                        restorePassword = ""
                        restoreError = null
                        message = null
                    } else {
                        val payload = withContext(Dispatchers.Default) { manager.validate(bytes, null) }
                        pendingRestore = payload
                        message = null
                    }
                } catch (e: BackupCrypto.BackupFormatException) {
                    message = e.message ?: "فایل نامعتبر"; isError = true
                } catch (e: Exception) {
                    message = "خواندن فایل ممکن نشد. داده فعلی تغییری نکرد."; isError = true
                } finally { busy = false }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "بکاپ یک فایل واحد و کامل از حساب‌ها، تراکنش‌ها، دارایی‌ها، تعهدات، یادآورها، پیامک‌ها، تصاویر خصوصی و تمام تنظیمات کاربر است. " +
                    "پس از حذف و نصب مجدد برنامه، بازیابی جایگزینی همه این اطلاعات را برمی‌گرداند. " +
                    "مجوزهای سیستمی اندروید باید دوباره توسط خود کاربر تأیید شوند.\n\n" +
                    "اگر رمزگذاری را روشن بگذارید، فایل با AES-256-GCM محافظت می‌شود و رمز آن مستقل از رمز گوشی است.\n\n" +
                    "مهم: اگر رمز بکاپ را فراموش کنید، بازیابی به هیچ روشی ممکن نیست. " +
                    "بکاپ بدون رمز راحت‌تر است ولی هرکسی که فایل را داشته باشد می‌تواند آن را بخواند.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = skin.onBackdrop.copy(alpha = 0.9f)
            )
        }

        Text("پشتیبان‌گیری آنلاین", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)
        SkinCard(modifier = Modifier.fillMaxWidth(), tonal = true, glow = true) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(17.dp))
                            .background(Brush.linearGradient(listOf(skin.accent, skin.accent.copy(alpha = .55f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(Icons.Filled.CloudSync, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("بکاپ امن روی حساب کاربری", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = skin.onBackdrop)
                        Text("همگام‌سازی رمزگذاری‌شده بین دستگاه‌ها", style = MaterialTheme.typography.bodySmall, color = skin.onBackdrop.copy(alpha = .72f))
                    }
                    androidx.compose.material3.Icon(Icons.Filled.Lock, null, tint = skin.accent, modifier = Modifier.size(20.dp))
                }
                Button(
                    onClick = { message = "به زودی افزوده خواهد شد"; isError = false; showSavedMessage(context, "به زودی افزوده خواهد شد") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    androidx.compose.material3.Icon(Icons.Filled.CloudSync, null)
                    Spacer(Modifier.size(8.dp))
                    Text("فعال‌سازی پشتیبان‌گیری آنلاین")
                }
            }
        }

        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        // ================= ساخت بکاپ =================
        Text("ساخت بکاپ", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)

        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("رمزگذاری فایل بکاپ", style = MaterialTheme.typography.bodyLarge, color = skin.onBackdrop)
                        Text(
                            if (encrypt) "فایل با رمز شما محافظت می‌شود" else "فایل بدون رمز ذخیره می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = skin.onBackdrop.copy(alpha = 0.7f)
                        )
                    }
                    Switch(checked = encrypt, onCheckedChange = { encrypt = it })
                }

                if (encrypt) {
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("رمز عبور بکاپ (حداقل ۶ کاراکتر)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().keepAboveKeyboard(), singleLine = true
                    )
                    OutlinedTextField(
                        value = passwordRepeat, onValueChange = { passwordRepeat = it },
                        label = { Text("تکرار رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().keepAboveKeyboard(), singleLine = true
                    )
                }

                Button(
                    enabled = !busy,
                    onClick = {
                        when {
                            encrypt && password.length < 6 -> { message = "رمز حداقل ۶ کاراکتر باشد"; isError = true }
                            encrypt && password != passwordRepeat -> { message = "تکرار رمز یکسان نیست"; isError = true }
                            else -> {
                                message = null
                                val date = ir.kharjyar.app.core.date.PersianDate.today()
                                    .format(persianDigits = false).replace("/", "-")
                                saveBackup.launch("kharjyar-backup-$date.khbk")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("انتخاب محل و ساخت بکاپ") }
            }
        }

        // ================= بازیابی =================
        Text("بازیابی", style = MaterialTheme.typography.titleMedium, color = skin.onBackdrop)

        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ابتدا فایل بکاپ را انتخاب کنید. اگر فایل رمزدار باشد، در مرحله بعد رمز آن پرسیده می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = skin.onBackdrop.copy(alpha = 0.75f)
                )
                OutlinedButton(
                    enabled = !busy,
                    onClick = { openBackup.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("انتخاب فایل بکاپ") }
            }
        }

        message?.let {
            Text(it, color = if (isError) MaterialTheme.colorScheme.error else skin.accent)
        }
        Spacer(Modifier.height(90.dp))
    }

    // ---------- مرحله ۲: پرسیدن رمز فقط برای فایل رمزدار ----------
    pendingBytes?.let { bytes ->
        AlertDialog(
            onDismissRequest = { pendingBytes = null; restorePassword = "" },
            containerColor = skin.dialogColor,
            title = { Text("رمز فایل بکاپ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("این فایل رمزدار است. رمز آن را وارد کنید.")
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it; restoreError = null },
                        label = { Text("رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = restoreError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    restoreError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = restorePassword.isNotBlank() && !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            try {
                                val payload = withContext(Dispatchers.Default) {
                                    manager.validate(bytes, restorePassword.toCharArray())
                                }
                                pendingRestore = payload
                                pendingBytes = null
                                restorePassword = ""
                            } catch (e: BackupCrypto.WrongPasswordOrCorruptException) {
                                restoreError = "رمز اشتباه است یا فایل آسیب دیده. داده فعلی تغییری نکرد."
                            } catch (e: Exception) {
                                restoreError = "باز کردن فایل ممکن نشد."
                            } finally { busy = false }
                        }
                    }
                ) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { pendingBytes = null; restorePassword = "" }) { Text("انصراف") }
            }
        )
    }

    // ---------- مرحله ۳: تأیید جایگزینی ----------
    pendingRestore?.let { payload ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            containerColor = skin.dialogColor,
            title = { Text("تأیید بازیابی") },
            text = {
                Text(
                    "فایل معتبر است.\n" +
                        "حساب‌ها: ${Digits.toPersian(payload.accounts.size.toString())}\n" +
                        "تراکنش‌ها: ${Digits.toPersian(payload.transactions.size.toString())}\n" +
                        "دسته‌ها: ${Digits.toPersian(payload.categories.size.toString())}\n" +
                        "قالب‌ها: ${Digits.toPersian(payload.templates.size.toString())}\n\n" +
                        "روش بازیابی را انتخاب کنید:\n\n" +
                        "۱) حذف اطلاعات موجود و بازیابی: اطلاعات فعلی پاک و محتوای فایل بکاپ جایگزین می‌شود.\n\n" +
                        "۲) ادغام با اطلاعات موجود: محتوای بکاپ بدون پاک‌کردن اطلاعات فعلی ترکیب می‌شود و پیامک‌های تکراری بر اساس fingerprint دوباره ثبت نمی‌شوند.\n\n" +
                        "۳) بکاپ ایمنی و بازیابی: ابتدا فایل جداگانه‌ای از تمام اطلاعات فعلی ذخیره می‌شود؛ سپس اطلاعات فعلی پاک و محتوای بکاپ جایگزین می‌شود."
                )
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = {
                        scope.launch {
                            busy = true
                            try {
                                manager.restore(payload)
                                message = "اطلاعات موجود حذف شد و بکاپ با موفقیت بازیابی شد."
                                isError = false
                            } catch (_: Exception) {
                                message = "بازیابی انجام نشد؛ اطلاعات موجود بدون تغییر باقی ماند."
                                isError = true
                            } finally {
                                busy = false
                                pendingRestore = null
                            }
                        }
                    }, enabled = !busy) { Text("حذف اطلاعات موجود و بازیابی", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = {
                        safetyBackupPayload = payload
                        val date = ir.kharjyar.app.core.date.PersianDate.today().format(persianDigits = false).replace("/", "-")
                        saveSafetyBackup.launch("kharjyar-before-restore-$date.khbk")
                    }, enabled = !busy) { Text("ابتدا بکاپ از اطلاعات موجود") }
                    TextButton(onClick = {
                        scope.launch {
                            busy = true
                            try {
                                val report = manager.merge(payload)
                                message = "اطلاعات بکاپ با اطلاعات موجود ادغام شد." +
                                    if (report.mergedDuplicates > 0) " ${Digits.toPersian(report.mergedDuplicates.toString())} پیامک تکراری نادیده گرفته شد." else ""
                                isError = false
                            } catch (_: Exception) {
                                message = "ادغام اطلاعات ناموفق بود؛ اطلاعات فعلی تغییر نکرد."
                                isError = true
                            } finally {
                                busy = false
                                pendingRestore = null
                            }
                        }
                    }, enabled = !busy) { Text("ادغام با اطلاعات موجود") }
                }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null; safetyBackupPayload = null }) { Text("انصراف") } }
        )
    }
}

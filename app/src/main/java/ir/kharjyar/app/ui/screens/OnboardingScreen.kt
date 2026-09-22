package ir.kharjyar.app.ui.screens

import android.Manifest
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

/**
 * راه‌اندازی اولیه: معرفی قابلیت‌ها، توضیح حریم خصوصی و درخواست مجوزها.
 * مجوزها اختیاری‌اند؛ ثبت دستی بدون هیچ مجوزی کار می‌کند.
 */
@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    var step by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    /** وضعیت واقعی مجوز را از خود سیستم می‌پرسد (نه از حافظه برنامه). */
    fun hasPermission(name: String): Boolean =
        ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED

    var smsGranted by remember { mutableStateOf(hasPermission(Manifest.permission.RECEIVE_SMS)) }
    var notifGranted by remember {
        mutableStateOf(
            // اعلان فقط از اندروید ۱۳ به بعد مجوز جدا دارد
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            else true
        )
    }

    // اگر کاربر از تنظیمات گوشی مجوز را عوض کند، با برگشت به برنامه دوباره استعلام می‌گیریم
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                smsGranted = hasPermission(Manifest.permission.RECEIVE_SMS)
                notifGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        hasPermission(Manifest.permission.POST_NOTIFICATIONS)
                    else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        smsGranted = it
    }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifGranted = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("💰", style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(24.dp))
        Text("خرج‌یار", style = MaterialTheme.typography.headlineLarge)
        Text(
            "همیار شخصی مدیریت درآمد و مخارج",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        when (step) {
            0 -> {
                FeatureCard(
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "خواندن خودکار پیامک بانکی",
                    text = "با اجازه شما، پیامک‌های بانکی جدید شناسایی و به پیش‌نویس تراکنش تبدیل می‌شوند. هیچ تراکنشی بدون تأیید شما قطعی نمی‌شود."
                )
                FeatureCard(
                    icon = { Icon(Icons.Filled.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "حریم خصوصی",
                    text = "همه پردازش‌ها روی همین گوشی انجام می‌شود. فقط پیامک‌های مالی ذخیره می‌شوند؛ رمز یک‌بارمصرف و پیامک شخصی ذخیره نمی‌شود. هیچ داده‌ای از برنامه به جایی ارسال نمی‌شود و برنامه اینترنت لازم ندارد."
                )
                FeatureCard(
                    icon = { Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "امنیت",
                    text = "قفل برنامه با اثر انگشت یا رمز دستگاه و بکاپ رمزنگاری‌شده در تنظیمات در دسترس است."
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("ادامه") }
            }
            1 -> {
                FeatureCard(
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "مجوز دریافت پیامک",
                    text = "برای تشخیص خودکار تراکنش‌ها لازم است. فقط پیامک‌های دریافتی جدید بررسی می‌شوند؛ تاریخچه پیامک‌ها خوانده نمی‌شود."
                )
                PermissionButton(
                    granted = smsGranted,
                    grantedText = "فعال است",
                    requestText = "درخواست مجوز پیامک",
                    onRequest = { smsPermission.launch(Manifest.permission.RECEIVE_SMS) }
                )
                Spacer(Modifier.height(12.dp))
                FeatureCard(
                    icon = { Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "مجوز اعلان",
                    text = "وقتی برنامه باز نیست، تراکنش جدید با اعلان به شما اطلاع داده می‌شود تا «برای چه بود؟» را تکمیل کنید."
                )
                PermissionButton(
                    granted = notifGranted,
                    grantedText = "فعال است",
                    requestText = "درخواست مجوز اعلان",
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "این مجوزها اختیاری‌اند؛ ثبت دستی تراکنش همیشه بدون مجوز کار می‌کند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { scope.launch { viewModel.settingsRepo.setOnboardingDone(true) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("شروع") }
            }
        }
    }
}

/**
 * دکمه مجوز: اگر مجوز داده شده باشد به حالت «فعال است» با تیک سبز تبدیل می‌شود
 * و دیگر قابل فشردن نیست. وضعیت از خود سیستم‌عامل خوانده می‌شود.
 */
@Composable
private fun PermissionButton(
    granted: Boolean,
    grantedText: String,
    requestText: String,
    onRequest: () -> Unit
) {
    if (granted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2E7D32).copy(alpha = 0.18f))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(grantedText, color = Color(0xFF4CAF50))
        }
    } else {
        OutlinedButton(onClick = onRequest, modifier = Modifier.fillMaxWidth()) { Text(requestText) }
    }
}

@Composable
private fun FeatureCard(icon: @Composable () -> Unit, title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            icon()
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

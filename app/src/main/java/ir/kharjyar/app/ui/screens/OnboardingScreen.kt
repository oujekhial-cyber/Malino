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

    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

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
                OutlinedButton(
                    onClick = { smsPermission.launch(Manifest.permission.RECEIVE_SMS) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("درخواست مجوز پیامک") }
                Spacer(Modifier.height(12.dp))
                FeatureCard(
                    icon = { Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary) },
                    title = "مجوز اعلان",
                    text = "وقتی برنامه باز نیست، تراکنش جدید با اعلان به شما اطلاع داده می‌شود تا «برای چه بود؟» را تکمیل کنید."
                )
                OutlinedButton(
                    onClick = { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("درخواست مجوز اعلان") }
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

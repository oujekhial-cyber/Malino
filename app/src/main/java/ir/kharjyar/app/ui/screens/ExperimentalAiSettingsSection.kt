package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ai.ExperimentalPersianAi
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

/** تنها نقطه UI قابلیت آزمایشی؛ حذف این فایل و یک سطر SettingsScreen آن را کامل کنار می‌گذارد. */
@Composable
fun ExperimentalAiSettingsSection(viewModel: AppViewModel, enabled: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tokenDraft by remember { mutableStateOf("") }
    var tokenSaved by remember { mutableStateOf(ExperimentalPersianAi.hasToken(context)) }
    var question by remember { mutableStateOf("به فارسی خودت را کوتاه معرفی کن و بگو به اطلاعات مالی من دسترسی نداری.") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    SectionCard("دستیار هوشمند آزمایشی") {
        Text(
            "مدل Qwen به‌صورت آنلاین از درگاه ایرانی AvalAI فراخوانی می‌شود. این نمونه هیچ دسترسی‌ای به پیامک، حساب، تراکنش یا پایگاه داده خرج‌یار ندارد.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "برای آزمایش، کلید API شخصی AvalAI را فقط روی همین گوشی وارد کنید. کلید با Android Keystore رمزگذاری می‌شود و داخل بکاپ قرار نمی‌گیرد.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SkinCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("فعال‌سازی اختیاری")
                    Text(
                        if (enabled) "دستیار فعال است" else "دستیار خاموش است و هیچ درخواست اینترنتی ارسال نمی‌کند",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    enabled = tokenSaved,
                    onCheckedChange = { value -> scope.launch { viewModel.settingsRepo.setExperimentalAiEnabled(value) } }
                )
            }
        }
        OutlinedTextField(
            value = tokenDraft,
            onValueChange = { tokenDraft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (tokenSaved) "جایگزینی کلید API" else "کلید API AvalAI") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    ExperimentalPersianAi.saveToken(context, tokenDraft)
                    tokenDraft = ""
                    tokenSaved = ExperimentalPersianAi.hasToken(context)
                    error = if (tokenSaved) "" else "کلید خالی است."
                },
                enabled = tokenDraft.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text("ذخیره امن کلید") }
            OutlinedButton(
                onClick = {
                    ExperimentalPersianAi.clearToken(context)
                    tokenSaved = false
                    tokenDraft = ""
                    answer = ""
                    scope.launch { viewModel.settingsRepo.setExperimentalAiEnabled(false) }
                },
                enabled = tokenSaved,
                modifier = Modifier.weight(1f)
            ) { Text("حذف کلید") }
        }
        if (tokenSaved) Text("کلید روی دستگاه ذخیره شده است.", color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("پیام آزمایشی") },
            minLines = 3,
            enabled = enabled && !loading
        )
        Button(
            onClick = {
                loading = true; answer = ""; error = ""
                scope.launch {
                    ExperimentalPersianAi.ask(context, question)
                        .onSuccess { answer = it }
                        .onFailure { error = it.message ?: "خطای ناشناخته در اتصال" }
                    loading = false
                }
            },
            enabled = enabled && tokenSaved && question.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("ارسال پیام آزمایشی")
        }
        if (answer.isNotBlank()) SkinCard(Modifier.fillMaxWidth()) {
            Text(answer, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
        }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Text(
            "این قابلیت آزمایشی، مستقل و قابل حذف است. خاموش‌کردن آن تمام درخواست‌ها را متوقف می‌کند؛ حذف کلید نیز اطلاعات اتصال را از گوشی پاک می‌کند.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

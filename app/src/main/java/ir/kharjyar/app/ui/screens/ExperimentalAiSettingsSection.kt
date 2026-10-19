package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import ir.kharjyar.app.ai.AiProvider
import ir.kharjyar.app.ai.ExperimentalPersianAi
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import kotlinx.coroutines.launch

/** تنظیمات فقط فعال‌سازی، انتخاب سرویس و کلید را نگه می‌دارد؛ فرمان‌ها در مرکز فرمان هستند. */
@Composable
fun ExperimentalAiSettingsSection(viewModel: AppViewModel, enabled: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var provider by remember { mutableStateOf(ExperimentalPersianAi.activeProvider(context)) }
    var tokenDraft by remember(provider) { mutableStateOf("") }
    var tokenSaved by remember(provider) { mutableStateOf(ExperimentalPersianAi.hasToken(context, provider)) }
    var message by remember { mutableStateOf("") }

    SectionCard("هوش مصنوعی") {
        Text("سرویس دلخواه را انتخاب کنید. فرمان‌های متنی و صوتی در «مرکز فرمان هوشمند» قرار دارند.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiProvider.entries.forEach { item ->
                if (provider == item) Button({ provider=item; ExperimentalPersianAi.setActiveProvider(context,item); tokenSaved=ExperimentalPersianAi.hasToken(context,item); message="" },Modifier.weight(1f)){Text(item.title)}
                else OutlinedButton({ provider=item; ExperimentalPersianAi.setActiveProvider(context,item); tokenSaved=ExperimentalPersianAi.hasToken(context,item); message="" },Modifier.weight(1f)){Text(item.title)}
            }
        }
        Text("مدل فعال: ${provider.model}", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        SkinCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("فعال‌سازی اختیاری")
                    Text(if(enabled)"فعال با ${provider.title}" else "خاموش؛ هیچ درخواست اینترنتی ارسال نمی‌شود",style=MaterialTheme.typography.labelSmall)
                }
                Switch(enabled=tokenSaved,checked=enabled,onCheckedChange={v->scope.launch{viewModel.settingsRepo.setExperimentalAiEnabled(v)}})
            }
        }
        OutlinedTextField(tokenDraft,{tokenDraft=it},Modifier.fillMaxWidth(),label={Text(if(tokenSaved)"جایگزینی کلید ${provider.title}" else "کلید API ${provider.title}")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button({ExperimentalPersianAi.saveToken(context,tokenDraft,provider);tokenDraft="";tokenSaved=ExperimentalPersianAi.hasToken(context,provider);message=if(tokenSaved)"کلید ${provider.title} امن ذخیره شد." else "کلید خالی است."},enabled=tokenDraft.isNotBlank(),modifier=Modifier.weight(1f)){Text("ذخیره امن")}
            OutlinedButton({ExperimentalPersianAi.clearToken(context,provider);tokenSaved=false;scope.launch{viewModel.settingsRepo.setExperimentalAiEnabled(false)};message="کلید ${provider.title} حذف شد."},enabled=tokenSaved,modifier=Modifier.weight(1f)){Text("حذف کلید")}
        }
        if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.primary)
        Text("کلیدهای Groq و AvalAI جداگانه با Android Keystore نگهداری می‌شوند. فقط سرویس انتخاب‌شده استفاده می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

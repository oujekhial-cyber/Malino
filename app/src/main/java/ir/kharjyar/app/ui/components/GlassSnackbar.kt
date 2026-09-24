package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.theme.LocalAppSkin

/**
 * پیام کوتاه «شیشه‌ای»: کادر کوچک و نیمه‌شفاف که پایین و وسط صفحه ظاهر می‌شود.
 *
 * جای Snackbar پیش‌فرض متریال را می‌گیرد؛ آن نسخه تمام‌عرض و مات بود و روی
 * کارت‌ها سنگین دیده می‌شد.
 */
@Composable
fun GlassSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.fillMaxWidth()
    ) { data ->
        GlassSnackbar(data)
    }
}

@Composable
private fun GlassSnackbar(data: SnackbarData) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(18.dp)
    // شیشه‌ای: پس‌زمینه با ۵۰٪ شفافیت و یک حاشیه نازک روشن
    val glass = (if (skin.dark) Color.Black else Color.White).copy(alpha = 0.5f)
    val onGlass = skin.onBackdrop

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(glass)
                .border(1.dp, onGlass.copy(alpha = 0.18f), shape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                data.visuals.message,
                style = MaterialTheme.typography.bodySmall,
                color = onGlass,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            data.visuals.actionLabel?.let { action ->
                Spacer(Modifier.width(12.dp))
                Text(
                    action,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = skin.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { data.performAction() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

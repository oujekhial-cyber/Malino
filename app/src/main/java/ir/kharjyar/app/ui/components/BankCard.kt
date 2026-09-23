package ir.kharjyar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity

/**
 * کارت حساب به شکل کارت عابربانک.
 *
 * فقط فیلدهایی نمایش داده می‌شوند که کاربر پرکرده است؛ فیلد خالی اصلاً ردیفش
 * رسم نمی‌شود تا کارت شلوغ نشود.
 *
 * @param selected کارت فعال (اطلاعات مالی همین حساب در داشبورد نشان داده می‌شود).
 * @param masked اگر true باشد، شماره‌ها نیمه‌پنهان می‌شوند.
 */
@Composable
fun BankCard(
    account: AccountEntity,
    balanceText: String?,
    balanceCaption: String?,
    selected: Boolean,
    masked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCopy: (String, String) -> Unit = { _, _ -> }
) {
    val base = Color(account.colorArgb)
    val shape = RoundedCornerShape(20.dp)
    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.25f,
        animationSpec = tween(220),
        label = "cardBorder"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        base.lighten(0.22f),
                        base,
                        base.darken(0.28f)
                    )
                )
            )
            .border(if (selected) 2.dp else 1.dp, Color.White.copy(alpha = borderAlpha), shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ---------- سربرگ: نام بانک و عنوان ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        account.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    if (account.bankName.isNotBlank()) {
                        Text(
                            account.bankName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---------- شماره کارت ----------
            if (account.cardNumber.isNotBlank()) {
                Text(
                    formatCardNumber(account.cardNumber, masked),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.clickable { onCopy("شماره کارت", account.cardNumber) }
                )
                Spacer(Modifier.height(10.dp))
            }

            // ---------- انقضا و CVV2 ----------
            if (account.cardExpiry.isNotBlank() || account.cardCvv2.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    if (account.cardExpiry.isNotBlank()) {
                        CardMiniField("انقضا", Digits.toPersian(account.cardExpiry))
                    }
                    if (account.cardCvv2.isNotBlank()) {
                        CardMiniField(
                            "CVV2",
                            if (masked) "•••" else Digits.toPersian(account.cardCvv2)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // ---------- شماره حساب و شبا ----------
            if (account.accountNumber.isNotBlank()) {
                CardLineField(
                    label = "حساب",
                    value = if (masked) maskTail(account.accountNumber) else Digits.toPersian(account.accountNumber),
                    onCopy = { onCopy("شماره حساب", account.accountNumber) }
                )
            }
            if (account.iban.isNotBlank()) {
                CardLineField(
                    label = "شبا",
                    value = if (masked) maskTail(account.iban) else "IR" + Digits.toPersian(account.iban),
                    onCopy = { onCopy("شماره شبا", "IR" + account.iban) }
                )
            }

            // ---------- مانده ----------
            if (balanceText != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.26f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            balanceText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (balanceCaption != null) {
                            Text(
                                balanceCaption,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardMiniField(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CardLineField(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCopy)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.width(34.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "کپی",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp)
        )
    }
}

/** ۱۶ رقم کارت را چهارتایی جدا می‌کند؛ در حالت پنهان فقط چهار رقم آخر می‌ماند. */
private fun formatCardNumber(raw: String, masked: Boolean): String {
    val digits = Digits.normalize(raw).filter(Char::isDigit)
    if (digits.isEmpty()) return ""
    val shown = if (masked && digits.length > 4) {
        "•".repeat(digits.length - 4) + digits.takeLast(4)
    } else digits
    return Digits.toPersian(shown.chunked(4).joinToString("  "))
}

/** فقط چهار کاراکتر آخر را نشان می‌دهد. */
private fun maskTail(raw: String): String {
    val t = raw.trim()
    if (t.length <= 4) return Digits.toPersian(t)
    return "••••" + Digits.toPersian(t.takeLast(4))
}

private fun Color.lighten(f: Float) = Color(
    red + (1f - red) * f,
    green + (1f - green) * f,
    blue + (1f - blue) * f,
    alpha
)

private fun Color.darken(f: Float) = Color(
    red * (1f - f),
    green * (1f - f),
    blue * (1f - f),
    alpha
)

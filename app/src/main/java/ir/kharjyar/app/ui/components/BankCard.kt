package ir.kharjyar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.text.Digits

/**
 * کارت حساب به شکل کارت عابربانک.
 *
 * رنگ کارت از رنگ انتخابی خود حساب گرفته می‌شود تا هر بانک ظاهر خودش را داشته باشد.
 * فیلدهایی که کاربر وارد نکرده باشد اصلاً نمایش داده نمی‌شوند.
 *
 * @param selected کارت فعال؛ حاشیه روشن و کمی بزرگ‌تر می‌شود.
 * @param balanceText مانده برآوردی، از قبل قالب‌بندی‌شده.
 */
@Composable
fun BankCard(
    title: String,
    bankName: String,
    colorArgb: Long,
    balanceText: String,
    balanceHint: String,
    selected: Boolean,
    cardNumber: String = "",
    accountNumber: String = "",
    iban: String = "",
    expiry: String = "",
    cvv2: String = "",
    showSecrets: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCopy: (String) -> Unit = {}
) {
    val base = Color(colorArgb)
    // گرادیان از رنگ حساب: روشن‌تر در بالا-راست، تیره‌تر در پایین-چپ
    val top = base.lighten(0.18f)
    val bottom = base.darken(0.32f)
    // متن روی کارت بر اساس روشنایی رنگ انتخاب می‌شود تا همیشه خوانا بماند
    val onCard = if (base.luminance() > 0.55f) Color(0xFF14121A) else Color.White
    val shape = RoundedCornerShape(20.dp)

    val borderColor by animateColorAsState(
        if (selected) onCard.copy(alpha = 0.85f) else Color.Transparent,
        tween(220),
        label = "cardBorder"
    )
    val borderWidth by animateDpAsState(
        if (selected) 2.dp else 0.dp,
        tween(220),
        label = "cardBorderWidth"
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(top, bottom)))
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // ---------- ردیف بالا: نام بانک و نشان ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onCard,
                    maxLines = 1
                )
                if (bankName.isNotBlank()) {
                    Text(
                        bankName,
                        style = MaterialTheme.typography.labelSmall,
                        color = onCard.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(onCard.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (selected) Icons.Filled.Check else Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = onCard,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- شماره کارت ----------
        if (cardNumber.isNotBlank()) {
            Text(
                formatCardNumber(cardNumber, showSecrets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onCard,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                modifier = Modifier.clickable { onCopy(cardNumber) }
            )
            Spacer(Modifier.height(10.dp))
        }

        // ---------- مانده ----------
        Text(
            balanceHint,
            style = MaterialTheme.typography.labelSmall,
            color = onCard.copy(alpha = 0.72f)
        )
        Text(
            balanceText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = onCard,
            maxLines = 1
        )

        // ---------- جزئیات، فقط وقتی کارت انتخاب شده ----------
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(200)) + expandVertically(tween(220)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(180))
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(onCard.copy(alpha = 0.22f))
                )
                Spacer(Modifier.height(10.dp))

                if (accountNumber.isNotBlank()) {
                    CardField("شماره حساب", Digits.toPersian(accountNumber), onCard) { onCopy(accountNumber) }
                }
                if (iban.isNotBlank()) {
                    CardField("شبا", "IR" + Digits.toPersian(iban), onCard) { onCopy("IR$iban") }
                }
                if (expiry.isNotBlank() || cvv2.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        if (expiry.isNotBlank()) {
                            Column {
                                Text(
                                    "انقضا",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onCard.copy(alpha = 0.7f)
                                )
                                Text(
                                    Digits.toPersian(expiry),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onCard
                                )
                            }
                        }
                        if (cvv2.isNotBlank()) {
                            Column {
                                Text(
                                    "CVV2",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onCard.copy(alpha = 0.7f)
                                )
                                Text(
                                    if (showSecrets) Digits.toPersian(cvv2) else "•••",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onCard
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardField(
    label: String,
    value: String,
    onCard: Color,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCopy)
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = onCard.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = onCard,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "کپی",
            tint = onCard.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}

/** گروه‌بندی چهارتایی شماره کارت؛ در حالت مخفی فقط چهار رقم آخر دیده می‌شود. */
private fun formatCardNumber(raw: String, reveal: Boolean): String {
    val digits = Digits.normalize(raw).filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val shown = if (reveal || digits.length <= 4) digits
    else "•".repeat(digits.length - 4) + digits.takeLast(4)
    return Digits.toPersian(shown.chunked(4).joinToString("  "))
}

private fun Color.lighten(f: Float) = Color(
    red + (1f - red) * f,
    green + (1f - green) * f,
    blue + (1f - blue) * f,
    alpha
)

private fun Color.darken(f: Float) = Color(red * (1f - f), green * (1f - f), blue * (1f - f), alpha)

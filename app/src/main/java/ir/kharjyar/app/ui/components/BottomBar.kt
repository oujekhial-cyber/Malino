package ir.kharjyar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.theme.LocalAppSkin

/**
 * بلندای بخشی از نوار پایین که بالای بدنه نوار بیرون می‌زند (نیمه بالایی دکمه وسط).
 * این ناحیه شفاف است، پس محتوا می‌تواند تا زیر آن ادامه پیدا کند.
 */
val BottomBarOverhang: Dp = 19.dp

/** یک آیتم نوار پایین. */
data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * نوار پایین تم‌پذیر با دکمه گرد «تراکنش جدید» که روی نوار می‌نشیند
 * (همان چیدمانی که در طرح‌های تم دیده می‌شود).
 */
@Composable
fun BottomNavBar(
    items: List<BottomItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    require(items.size == 4) { "نوار پایین برای دقیقاً چهار آیتم طراحی شده است" }

    // ابعاد فشرده: نوار کوتاه‌تر از قبل تا فضای صفحه بیشتر بماند.
    val fabSize = 56.dp
    val fabRadius = fabSize / 2
    val gap = 7.dp
    val notchRadius = fabRadius + gap
    val shoulder = 9.dp
    // لبه بالای نوار دقیقاً به اندازه «shoulder» بالاتر از مرکز دکمه است تا
    // دکمه درست وسط قوس بنشیند و گوشه‌های دو طرف قوس گرد و پیوسته باشند.
    val barTop = fabRadius - shoulder
    val barShape = remember(notchRadius, shoulder) {
        NotchedBarShape(notchRadius = notchRadius, shoulder = shoulder, corner = 26.dp)
    }
    // خط مرزی نوار: هم بالای نوار و هم دور قوسِ دکمه دیده می‌شود.
    val borderColor = skin.accent.copy(alpha = 0.45f)
    val dividerColor = skin.navUnselected.copy(alpha = 0.35f)

    Box(modifier = modifier.fillMaxWidth()) {
        // نوار محوکنندهٔ قبلی (۵۶dp) حذف شد؛ چون مثل یک حاشیه سیاه، محتوای صفحه را
        // پیش از رسیدن به نوار پنهان می‌کرد. حالا محتوا تا خودِ خط نوار دیده می‌شود.

        // بدنه نوار با قوس (بریدگی) دور دکمه مرکزی
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = barTop)
                .clip(barShape)
                .background(skin.navBarColor)
                .border(1.dp, borderColor, barShape)
                .navigationBarsPadding()
                .padding(top = 7.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavCell(items[0], currentRoute == items[0].route, Modifier.weight(1f)) { onSelect(items[0].route) }
            NavDivider(dividerColor)
            NavCell(items[1], currentRoute == items[1].route, Modifier.weight(1f)) { onSelect(items[1].route) }
            // جای خالی برای دکمه مرکزی (به اندازه دهانه قوس)
            Spacer(Modifier.width(notchRadius * 2))
            NavCell(items[2], currentRoute == items[2].route, Modifier.weight(1f)) { onSelect(items[2].route) }
            NavDivider(dividerColor)
            NavCell(items[3], currentRoute == items[3].route, Modifier.weight(1f)) { onSelect(items[3].route) }
        }

        // هاله نور رنگ تم دور دکمه
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = fabRadius - 46.dp)
                .size(92.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            skin.accent.copy(alpha = 0.30f),
                            skin.accent.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        radius = with(LocalDensity.current) { 46.dp.toPx() }
                    ),
                    CircleShape
                )
        )

        // دکمه گرد مرکزی «تراکنش جدید» که داخل قوس می‌نشیند
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(fabSize)
                .clip(CircleShape)
                .background(Brush.linearGradient(skin.fabGradient))
                .border(1.dp, borderColor, CircleShape)
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "ثبت تراکنش جدید",
                tint = if (skin.dark && skin.fabGradient.first().luminance() > 0.6f) Color(0xFF12241D) else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/** خط عمودی نازک بین آیتم‌های نوار پایین. */
@Composable
private fun NavDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(color)
    )
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

/**
 * شکل نوار پایین: گوشه‌های گرد بالا و یک قوس نیم‌دایره‌ای واقعی در وسط.
 *
 * دو طرف قوس با کمان کوچکی (shoulder) به لبه صاف نوار وصل می‌شود تا گوشه‌ها
 * تیز نباشند و انحنا پیوسته دیده شود؛ دکمه شناور دقیقاً در مرکز قوس می‌نشیند.
 *
 * @param notchRadius شعاع قوس (شعاع دکمه + فاصله هوایی).
 * @param shoulder شعاع گردی دو طرف قوس.
 * @param corner شعاع گوشه‌های بالای نوار.
 */
private class NotchedBarShape(
    private val notchRadius: Dp,
    private val shoulder: Dp,
    private val corner: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { notchRadius.toPx() }
        val s = with(density) { shoulder.toPx() }
        val c = with(density) { corner.toPx() }
        val cx = size.width / 2f

        val path = Path().apply {
            moveTo(0f, c)
            // گوشه گرد بالا-چپ
            arcTo(Rect(0f, 0f, 2 * c, 2 * c), 180f, 90f, false)
            lineTo(cx - r - s, 0f)
            // گردی ورودی قوس (مماس بر لبه صاف و بر خود قوس)
            arcTo(Rect(cx - r - 2 * s, 0f, cx - r, 2 * s), 270f, 90f, false)
            // نیم‌دایره قوس؛ مرکز آن مرکز دکمه است
            arcTo(Rect(cx - r, s - r, cx + r, s + r), 180f, -180f, false)
            // گردی خروجی قوس
            arcTo(Rect(cx + r, 0f, cx + r + 2 * s, 2 * s), 180f, 90f, false)
            lineTo(size.width - c, 0f)
            // گوشه گرد بالا-راست
            arcTo(Rect(size.width - 2 * c, 0f, size.width, 2 * c), 270f, 90f, false)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun NavCell(
    item: BottomItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val skin = LocalAppSkin.current
    val tint by animateColorAsState(
        if (selected) skin.navSelected else skin.navUnselected,
        tween(220),
        label = "navTint"
    )
    val scale by animateFloatAsState(if (selected) 1.1f else 1f, tween(220), label = "navScale")
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(22.dp).scale(scale)
            )
            if (item.badgeCount > 0) {
                Badge(
                    modifier = Modifier.padding(start = 10.dp),
                    containerColor = skin.expenseColor
                ) { Text(Digits.toPersian(item.badgeCount.toString())) }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}

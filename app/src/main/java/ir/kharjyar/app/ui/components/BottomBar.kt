package ir.kharjyar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.theme.LocalAppSkin

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

    Box(modifier = modifier.fillMaxWidth()) {
        // بدنه نوار با قوس (بریدگی) دور دکمه مرکزی
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 31.dp)
                .clip(NotchedBarShape(fabRadius = 33.dp, gap = 7.dp, corner = 26.dp))
                .background(skin.navBarColor)
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // دو آیتم اول
            items.take(2).forEach { item ->
                NavCell(item, currentRoute == item.route, Modifier.weight(1f)) { onSelect(item.route) }
            }
            // جای خالی برای دکمه مرکزی
            Spacer(Modifier.width(76.dp))
            items.drop(2).forEach { item ->
                NavCell(item, currentRoute == item.route, Modifier.weight(1f)) { onSelect(item.route) }
            }
        }

        // دکمه گرد مرکزی «تراکنش جدید» که داخل قوس می‌نشیند
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(62.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(skin.fabGradient))
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "ثبت تراکنش جدید",
                tint = if (skin.dark && skin.fabGradient.first().luminance() > 0.6f) Color(0xFF12241D) else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

/**
 * شکل نوار پایین با گوشه‌های گرد بالا و یک قوس نیم‌دایره‌ای در وسط
 * که دکمه شناور دقیقاً داخل آن می‌نشیند.
 *
 * @param fabRadius شعاع دکمه شناور.
 * @param gap فاصله هوایی بین دکمه و لبه قوس.
 * @param corner شعاع گوشه‌های بالای نوار.
 */
private class NotchedBarShape(
    private val fabRadius: Dp,
    private val gap: Dp,
    private val corner: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { (fabRadius + gap).toPx() }
        val c = with(density) { corner.toPx() }
        val cx = size.width / 2f
        // شعاع منحنی‌های کوچکِ وصل‌کننده در دو طرف قوس (برای گذار نرم)
        val ease = r * 0.55f

        val path = Path().apply {
            moveTo(0f, c)
            // گوشه گرد بالا-چپ
            quadraticBezierTo(0f, 0f, c, 0f)
            // خط تا شروع گذار قوس
            lineTo(cx - r - ease, 0f)
            // گذار نرم از خط افقی به لبه قوس
            cubicTo(
                cx - r - ease * 0.4f, 0f,
                cx - r - ease * 0.1f, 0f,
                cx - r, 0f
            )
            // قوس نیم‌دایره‌ای رو به پایین (۱۸۰° → ۰° از مسیر پایین)
            arcTo(
                rect = Rect(cx - r, -r, cx + r, r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            // گذار نرم از لبه قوس به خط افقی
            cubicTo(
                cx + r + ease * 0.1f, 0f,
                cx + r + ease * 0.4f, 0f,
                cx + r + ease, 0f
            )
            lineTo(size.width - c, 0f)
            // گوشه گرد بالا-راست
            quadraticBezierTo(size.width, 0f, size.width, c)
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
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(24.dp).scale(scale)
            )
            if (item.badgeCount > 0) {
                Badge(
                    modifier = Modifier.padding(start = 10.dp),
                    containerColor = skin.expenseColor
                ) { Text(Digits.toPersian(item.badgeCount.toString())) }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
        // نقطه نشانگر انتخاب
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(if (selected) 5.dp else 0.dp)
                .clip(CircleShape)
                .background(if (selected) skin.navSelected else Color.Transparent)
        )
    }
}

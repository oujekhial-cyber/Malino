package ir.kharjyar.app.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.theme.AppSkin
import ir.kharjyar.app.ui.theme.LocalAppSkin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * پس‌زمینه تم‌پذیر برنامه: گرادیان + لکه‌های نوری/موج‌های محو.
 */
@Composable
fun AppBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val skin = LocalAppSkin.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(skin.backgroundColors.firstOrNull() ?: Color.Black)
    ) {
        // تصویر پس‌زمینه اختصاصی تم (در صورت وجود)
        skin.backdropImage?.let { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = skin.backdropImageAlpha,
                modifier = Modifier.fillMaxSize()
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (skin.backdropImage == null) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = skin.backgroundColors.ifEmpty { listOf(Color.Black, Color.Black) },
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
            }
            drawBlobs(skin)
        }
        content()
    }
}

private fun DrawScope.drawBlobs(skin: AppSkin) {
    val minSide = minOf(size.width, size.height)
    skin.backdropBlobs.forEach { blob ->
        val r = minSide * blob.radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blob.color, Color.Transparent),
                center = Offset(size.width * blob.cx, size.height * blob.cy),
                radius = r
            ),
            radius = r,
            center = Offset(size.width * blob.cx, size.height * blob.cy)
        )
    }
}

/**
 * کارت تم‌پذیر: نیمه‌شفاف با حاشیه نازک/گرادیانی و گوشه‌های تم.
 */
@Composable
fun SkinCard(
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)
    val border = Brush.linearGradient(
        if (skin.cardBorderColors.size >= 2) skin.cardBorderColors
        else skin.cardBorderColors + skin.cardBorderColors
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (tonal) Brush.linearGradient(skin.heroGradient)
                else Brush.linearGradient(
                    listOf(
                        skin.cardColor.copy(alpha = skin.cardAlpha),
                        skin.cardColor.copy(alpha = (skin.cardAlpha - 0.06f).coerceAtLeast(0f))
                    )
                )
            )
            .border(skin.cardBorderWidth, border, shape),
        content = content
    )
}

/**
 * کارت شاخص (موجودی کل): گرادیان تم به‌علاوه تصویر پس‌زمینه اختصاصی تم در صورت وجود.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(skin.heroGradient))
            .border(
                skin.cardBorderWidth,
                Brush.linearGradient(skin.cardBorderColors.ifEmpty { listOf(Color.Transparent, Color.Transparent) }),
                shape
            )
    ) {
        skin.heroImage?.let { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            // لایه تیره/روشن ملایم تا متن روی تصویر خوانا بماند
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                (if (skin.onHero.luminance() > 0.5f) Color.Black else Color.White)
                                    .copy(alpha = 0.10f)
                            )
                        )
                    )
            )
        }
        content()
    }
}

/** انیمیشن ورود کارت‌ها: محو‌شدن + لغزش ملایم از پایین، با تأخیر ترتیبی. */
@Composable
fun EnterCard(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val delay = (index.coerceIn(0, 8)) * 45
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(340, delayMillis = delay)) +
            slideInVertically(tween(380, delayMillis = delay)) { it / 6 }
    ) { content() }
}

/** سطح دیالوگ هماهنگ با تم. */
@Composable
fun SkinDialogSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val skin = LocalAppSkin.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(skin.cardCorner),
        color = skin.dialogColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        content = content
    )
}

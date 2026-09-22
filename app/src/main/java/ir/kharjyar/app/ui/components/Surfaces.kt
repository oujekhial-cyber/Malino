package ir.kharjyar.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
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
    /** هاله ملایم پیرامون کارت با رنگ خود کارت. */
    glow: Boolean = true,
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
            .then(
                if (glow) Modifier.softGlow(
                    color = if (tonal) skin.heroGradient.first() else skin.cardColor,
                    shape = shape
                ) else Modifier
            )
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
    /** نور نقطه‌ای که آرام دور کادر می‌چرخد. */
    orbit: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)
    Box(
        modifier = modifier
            .softGlow(skin.heroGradient.first(), shape, radius = 22.dp, intensity = 0.75f)
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
            // لایه هم‌رنگ‌سازی سبک: فقط آن‌قدر که متن خوانا بماند.
            // عمداً کم‌رنگ است تا حالت شیشه‌ای حفظ شود و نقش تصویر تم دیده شود.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                skin.heroGradient.first().copy(alpha = 0.14f),
                                skin.heroGradient.last().copy(alpha = 0.26f)
                            )
                        )
                    )
            )
            // سایه ملایم از پایین، تا اعداد بزرگ روی نقش تصویر گم نشوند
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.22f)
                        )
                    )
            )
        }
        // نور نقطه‌ای چرخان: روی تصویر می‌نشیند ولی زیر متن‌ها می‌ماند
        if (orbit) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .orbitGlow(
                        enabled = true,
                        color = Color.White,
                        cornerRadius = skin.cardCorner
                    )
            )
        }
        content()
    }
}

/**
 * هاله نور شیشه‌ای که آرام روی کارت حرکت می‌کند.
 * یک نوار مورب نیمه‌شفاف است که از یک لبه وارد و از لبه دیگر خارج می‌شود؛
 * چرخه کند و شفافیت پایین است تا حواس‌پرت‌کننده نباشد.
 */
@Composable
fun Modifier.shine(enabled: Boolean, cornerRadius: Dp): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "shine")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineProgress"
    )
    return this.drawWithContent {
        drawContent()
        // نوار نور از چپِ بیرون کادر تا راستِ بیرون کادر سفر می‌کند
        val travel = size.width * 2f
        val x = -size.width * 0.6f + travel * progress
        val bandWidth = size.width * 0.42f
        clipPath(
            Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset.Zero, size),
                        cornerRadius = CornerRadius(cornerRadius.toPx())
                    )
                )
            }
        ) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.012f),
                        Color.White.copy(alpha = 0.032f),
                        Color.White.copy(alpha = 0.012f),
                        Color.Transparent
                    ),
                    start = Offset(x, 0f),
                    end = Offset(x + bandWidth, size.height)
                )
            )
        }
    }
}

/**
 * هاله نرم پیرامون یک سطح، هم‌رنگ خودش.
 *
 * از سایه سخت استفاده نمی‌شود؛ shadow با رنگ روشن روی پس‌زمینه تیره لبه کثیف
 * می‌سازد. در عوض چند لایه کم‌رنگ با شعاع فزاینده پشت کارت کشیده می‌شود که
 * نتیجه‌اش پخش نرم نور است.
 */
fun Modifier.softGlow(
    color: Color,
    shape: androidx.compose.ui.graphics.Shape,
    radius: Dp = 18.dp,
    intensity: Float = 0.5f
): Modifier = this.drawBehind {
    val r = radius.toPx()
    val corner = ((shape as? RoundedCornerShape)
        ?.topStart?.toPx(size, this) ?: 0f)
    // سه لایه از بیرون به داخل، هر کدام کمی پررنگ‌تر
    val layers = listOf(1f to 0.05f, 0.62f to 0.09f, 0.3f to 0.14f)
    layers.forEach { (spread, alpha) ->
        val grow = r * spread
        drawRoundRect(
            color = color.copy(alpha = alpha * intensity),
            topLeft = Offset(-grow, -grow * 0.6f),
            size = Size(size.width + grow * 2, size.height + grow * 1.2f),
            cornerRadius = CornerRadius(corner + grow)
        )
    }
}

/**
 * نور نقطه‌ای که آرام دور محیط کارت می‌چرخد.
 *
 * نقطه روی دور مستطیل گرد حرکت می‌کند و یک هاله شعاعی نرم با خود می‌برد؛
 * شبیه بازتاب نوری که لبه شیشه را دور می‌زند.
 *
 * @param enabled خاموش/روشن از تنظیمات.
 * @param periodMillis زمان یک دور کامل. عدد بزرگ‌تر یعنی حرکت آرام‌تر.
 */
@Composable
fun Modifier.orbitGlow(
    enabled: Boolean,
    color: Color,
    cornerRadius: Dp,
    periodMillis: Int = 9000
): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "orbit")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitProgress"
    )
    return this.drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val perimeter = 2 * (w + h)
        val d = t * perimeter
        // تبدیل فاصله طی‌شده روی محیط به مختصات نقطه
        val p = when {
            d < w -> Offset(d, 0f)
            d < w + h -> Offset(w, d - w)
            d < 2 * w + h -> Offset(w - (d - w - h), h)
            else -> Offset(0f, h - (d - 2 * w - h))
        }
        val glowRadius = minOf(w, h) * 0.42f
        clipPath(
            Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset.Zero, size),
                        cornerRadius = CornerRadius(cornerRadius.toPx())
                    )
                )
            }
        ) {
            // هاله نرم دور نقطه
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.30f),
                        color.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = p,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = p
            )
            // مغز روشن نقطه
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
                    center = p,
                    radius = glowRadius * 0.22f
                ),
                radius = glowRadius * 0.22f,
                center = p
            )
        }
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

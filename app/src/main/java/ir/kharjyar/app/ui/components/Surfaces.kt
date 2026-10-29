package ir.kharjyar.app.ui.components

import android.os.SystemClock
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
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
    /** هاله ملایم پیرامون کارت. پیش‌فرض خاموش است؛ نور فقط برای کارت اصلی است. */
    glow: Boolean = false,
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
    /** قاب نئونی دور کارت. رنگش از تم فعال می‌آید. */
    neon: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)

    // لایه بیرونی بریده نمی‌شود تا هاله نئون بتواند از کادر بزند بیرون
    Box(
        modifier = modifier.then(
            if (neon && skin.neonColors.isNotEmpty()) {
                Modifier.neonFrame(skin.neonColors, skin.cardCorner)
            } else Modifier
        )
    ) {
        // بدنه کارت: این یکی clip می‌شود تا تصویر و محتوا داخل کادر بمانند
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Brush.linearGradient(skin.heroGradient))
                .then(
                    if (!skin.dark) Modifier.border(
                        width = skin.cardBorderWidth.coerceAtLeast(1.dp),
                        color = skin.cardBorderColors.firstOrNull()?.copy(alpha = 0.72f)
                            ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        shape = shape
                    ) else Modifier
                )
        ) {
            skin.heroImage?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // سایه ملایم از پایین، تا اعداد بزرگ روی نقش تصویر گم نشوند
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.35f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.34f)
                            )
                        )
                )
            }
            content()
        }
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
 * قاب نئونی دور کارت اصلی.
 *
 * مثل یک لوله نئون واقعی ساخته می‌شود: چند دور خطِ هم‌مرکز با ضخامت فزاینده و
 * شفافیت کاهشی، به‌علاوه یک مغز روشن باریک روی خود لبه. چون همه دورها با
 * drawRoundRect و همان شعاع گوشه رسم می‌شوند، منحنی گوشه‌ها کاملاً نرم می‌ماند و
 * هیچ گوشه تیز یا پله‌ای دیده نمی‌شود.
 *
 * رنگ‌ها از خود تم می‌آیند، پس هر تم نئون مخصوص خودش را دارد.
 *
 * @param colors دو رنگ گرادیان نئون؛ اگر خالی باشد چیزی رسم نمی‌شود.
 * @param spread پهنای پخش نور به بیرون.
 */
fun Modifier.neonFrame(
    colors: List<Color>,
    cornerRadius: Dp,
    spread: Dp = 13.dp
): Modifier = this.drawBehind {
    if (colors.isEmpty()) return@drawBehind
    val corner = cornerRadius.toPx()
    val s = spread.toPx()
    val brush = Brush.linearGradient(
        colors = if (colors.size >= 2) colors else colors + colors,
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )

    // هاله محو: لایه‌های بسیار نازک و پرتعداد.
    // قبلاً چند لایهٔ ضخیم بود و مرز هر لایه به شکل خط صاف دیده می‌شد؛
    // با ضخامت کم و گام ریز، گذار پیوسته و بدون خط می‌شود.
    val steps = 26
    for (i in steps downTo 1) {
        val k = i / steps.toFloat()
        val g = s * k
        // شدت با توان دو زیاد می‌شود: نزدیک لبه پررنگ، دورتر سریع محو
        val alpha = 0.085f * (1f - k) * (1f - k) + 0.012f
        drawRoundRect(
            brush = brush,
            topLeft = Offset(-g, -g),
            size = Size(size.width + g * 2, size.height + g * 2),
            cornerRadius = CornerRadius(corner + g),
            alpha = alpha,
            style = Stroke(width = s / steps * 2.4f)
        )
    }
}

/**
 * زمان (uptime) باز شدن صفحه. کارت‌هایی که بعد از باز شدن صفحه ساخته می‌شوند —
 * یعنی همان‌هایی که حین اسکرول وارد دید می‌شوند — دیگر انیمیشن ورود بازی
 * نمی‌کنند و بی‌درنگ دیده می‌شوند. مقدار منفی یعنی صفحه این را تنظیم نکرده است.
 */
val LocalScreenEnterTime = compositionLocalOf { -1L }

/** صفحه را علامت می‌زند تا انیمیشن ورود فقط برای نخستین نمایش اجرا شود. */
@Composable
fun ScreenEnterAnimation(content: @Composable () -> Unit) {
    val start = remember { SystemClock.uptimeMillis() }
    CompositionLocalProvider(LocalScreenEnterTime provides start) { content() }
}

/**
 * انیمیشن ورود کارت‌ها: محو‌شدن + لغزش کوتاه از پایین.
 *
 * زمان‌ها عمداً کوتاه‌اند (‎۱۷۰/۱۹۰ میلی‌ثانیه با گام ترتیبی ۱۸ میلی‌ثانیه) تا
 * فهرست تراکنش‌ها «تنبل» به نظر نرسد. اگر کارت حین اسکرول ساخته شود، انیمیشن
 * اصلاً اجرا نمی‌شود و ردیف فوری سر جایش است.
 */
@Composable
fun EnterCard(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenStart = LocalScreenEnterTime.current
    // پنجره کوتاه باز شدن صفحه؛ بعد از آن هر چیزی که ساخته شود یعنی کاربر
    // دارد اسکرول می‌کند.
    val firstPaint = remember {
        screenStart < 0L || SystemClock.uptimeMillis() - screenStart < 450L
    }
    var visible by remember { mutableStateOf(!firstPaint) }
    LaunchedEffect(Unit) { visible = true }
    val delay = if (firstPaint) (index.coerceIn(0, 6)) * 18 else 0
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(170, delayMillis = delay)) +
            slideInVertically(tween(190, delayMillis = delay)) { it / 8 }
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

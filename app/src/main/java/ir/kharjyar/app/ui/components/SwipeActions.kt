package ir.kharjyar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** بیشترین جابه‌جایی کارت هنگام کشیدن. */
private val SwipeMaxTravel = 112.dp

/** از این مقدار به بعد، رها کردن انگشت کار را انجام می‌دهد. */
private val SwipeTrigger = 82.dp

/**
 * ردیفی که با کشیدن انگشت، حذف یا ویرایش می‌شود.
 *
 * - کشیدن کارت به چپ: نوار «حذف» از سمت راست بیرون می‌آید.
 * - کشیدن کارت به راست: نوار «ویرایش» از سمت چپ بیرون می‌آید.
 *
 * چند نکته‌ای که این پیاده‌سازی دستی را لازم کرده است:
 *
 * 1. کارت هیچ‌وقت در حالت کشیده‌شده گیر نمی‌کند؛ بعد از برداشتن انگشت همیشه
 *    به جای خودش برمی‌گردد (چه کار انجام شده باشد، چه نشده باشد). قبلاً با
 *    `SwipeToDismissBox` اگر ردیف واقعاً از فهرست برداشته نمی‌شد، همان‌جا باز
 *    می‌ماند.
 * 2. تصمیم فقط بر اساس «مسافت» گرفته می‌شود، نه سرعت. بنابراین وقتی فهرست را
 *    عمودی بالا و پایین می‌کنید، یک تکان افقی تند دیگر ردیف را حذف نمی‌کند؛
 *    باید واقعاً انگشت را حدود نصف عرض یک کارت به پهلو بکشید.
 * 3. آیکون و برچسب از همان میلی‌متر اول کشیدن دیده می‌شوند و در همان سمتی
 *    هستند که کارت از رویشان کنار رفته است (چیدمان با `LayoutDirection.Ltr`
 *    قفل شده تا راست و چپ در صفحه راست‌به‌چپ جابه‌جا نشود).
 */
@Composable
fun SwipeActionRow(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    deleteLabel: String = "حذف",
    editLabel: String = "ویرایش",
    /**
     * فقط برای سازگاری با فراخوانی‌های قبلی نگه داشته شده است؛ ردیف در هر حالت
     * به جای خودش برمی‌گردد و برداشتن آن از فهرست بر عهده خود صفحه است.
     */
    @Suppress("UNUSED_PARAMETER") removeOnDelete: Boolean = true,
    content: @Composable () -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val maxTravel = with(density) { SwipeMaxTravel.toPx() }
    val trigger = with(density) { SwipeTrigger.toPx() }

    val offsetX = remember { Animatable(0f) }
    var armed by remember { mutableStateOf(false) }

    // اگر کشیدن غیرفعال شد (مثلاً حالت انتخاب چندتایی روشن شد) کارت باز نماند.
    LaunchedEffect(enabled) {
        if (!enabled && offsetX.value != 0f) offsetX.animateTo(0f, tween(160))
    }

    val dragState = rememberDraggableState { delta ->
        scope.launch {
            val next = (offsetX.value + delta).coerceIn(-maxTravel, maxTravel)
            offsetX.snapTo(next)
            val nowArmed = abs(next) >= trigger
            if (nowArmed != armed) {
                armed = nowArmed
                if (nowArmed) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // فقط وقتی جهت عوض می‌شود دوباره ساخته می‌شود؛ خود جابه‌جایی در مرحله
    // چیدمان/ترسیم خوانده می‌شود تا هر فریم recomposition نشود.
    val side by remember {
        derivedStateOf {
            val v = offsetX.value
            if (v > 0f) 1 else if (v < 0f) -1 else 0
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (side != 0) {
            val towardRight = side > 0
            // کارت به چپ رفته یعنی نوار سمت راست باز شده است.
            val deleting = !towardRight
            val tint = if (deleting) skin.expenseColor else skin.accent
            // اندازه‌گیری هم‌اندازه خود ردیف باید بیرون از CompositionLocalProvider
            // گرفته شود، چون آن‌جا دیگر داخل BoxScope نیستیم.
            val stripSize = Modifier.matchParentSize()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = stripSize
                        .clip(shape)
                        .graphicsLayer {
                            alpha = (abs(offsetX.value) / trigger).coerceIn(0.35f, 1f)
                        }
                        .background(tint.copy(alpha = 0.22f)),
                    contentAlignment = if (towardRight) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Row(
                        modifier = Modifier.clipToBounds().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = @Composable {
                            Icon(
                                if (deleting) Icons.Filled.Delete else Icons.Filled.Edit,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        val label = @Composable {
                            Text(
                                if (deleting) deleteLabel else editLabel,
                                color = tint,
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        // آیکون همیشه نزدیک لبه صفحه است تا اول از همه دیده شود.
                        if (towardRight) {
                            icon(); Spacer(Modifier.width(8.dp)); label()
                        } else {
                            label(); Spacer(Modifier.width(8.dp)); icon()
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .absoluteOffset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                    onDragStopped = {
                        val settled = offsetX.value
                        armed = false
                        offsetX.animateTo(0f, tween(180))
                        when {
                            settled <= -trigger -> onDelete()
                            settled >= trigger -> onEdit()
                        }
                    }
                )
        ) {
            content()
        }
    }
}

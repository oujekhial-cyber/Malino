package ir.kharjyar.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.theme.LocalAppSkin

/**
 * ردیف انتخاب تاریخ و ساعت: به‌جای فیلدهای عددی، دو دکمه که
 * پنجره تقویم شمسی و پنجره انتخاب ساعت را باز می‌کنند.
 */
@Composable
fun DateTimeField(
    date: PersianDate,
    hour: Int,
    minute: Int,
    onDate: (PersianDate) -> Unit,
    onTime: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text("تاریخ و ساعت (شمسی)", style = MaterialTheme.typography.labelLarge, color = skin.onBackdrop)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PickerButton(
                icon = Icons.Filled.CalendarMonth,
                text = "${Digits.toPersian(date.day.toString())} ${date.monthName()} ${Digits.toPersian(date.year.toString())}",
                modifier = Modifier.weight(1.6f),
                onClick = { showDate = true }
            )
            PickerButton(
                icon = Icons.Filled.Schedule,
                text = Digits.toPersian("%02d:%02d".format(hour, minute)),
                modifier = Modifier.weight(1f),
                onClick = { showTime = true }
            )
        }
    }

    if (showDate) {
        PersianDatePickerDialog(
            initial = date,
            onDismiss = { showDate = false },
            onConfirm = { picked -> onDate(picked); showDate = false }
        )
    }
    if (showTime) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTime = false },
            onConfirm = { h, m -> onTime(h, m); showTime = false }
        )
    }
}

@Composable
private fun PickerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(skin.cardColor.copy(alpha = skin.cardAlpha))
            .border(1.dp, skin.accent.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = skin.accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = skin.onBackdrop)
    }
}

/**
 * پنجره تقویم شمسی: شبکه ماه با شروع هفته از شنبه،
 * پیمایش ماه به ماه و میان‌بر «امروز».
 */
@Composable
fun PersianDatePickerDialog(
    initial: PersianDate,
    onDismiss: () -> Unit,
    onConfirm: (PersianDate) -> Unit
) {
    val skin = LocalAppSkin.current
    val today = remember { PersianDate.today() }
    var viewYear by remember { mutableStateOf(initial.year) }
    var viewMonth by remember { mutableStateOf(initial.month) }
    var selected by remember { mutableStateOf(initial) }

    fun shiftMonth(delta: Int) {
        val shifted = PersianDate(viewYear, viewMonth, 1).plusMonths(delta)
        viewYear = shifted.year
        viewMonth = shifted.month
    }

    val monthLength = PersianDate.monthLength(viewYear, viewMonth)
    // اندیس ستون اولین روز ماه (۰ = شنبه)
    val firstWeekday = remember(viewYear, viewMonth) {
        val first = PersianDate(viewYear, viewMonth, 1)
        (first.toLocalDate().dayOfWeek.value + 1) % 7
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = skin.dialogColor,
        title = null,
        text = {
            Column {
                // ---------- سربرگ ماه ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // در RTL این دکمه سمت راست دیده می‌شود و ماه را عقب می‌برد
                    NavArrow(Icons.Filled.ChevronRight) { shiftMonth(-1) }
                    Text(
                        "${PersianDate.MONTH_NAMES[viewMonth - 1]} ${Digits.toPersian(viewYear.toString())}",
                        style = MaterialTheme.typography.titleMedium,
                        color = skin.onBackdrop
                    )
                    NavArrow(Icons.Filled.ChevronLeft) { shiftMonth(1) }
                }

                Spacer(Modifier.height(10.dp))

                // ---------- نام روزهای هفته ----------
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { d ->
                        Text(
                            d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = skin.onBackdrop.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // ---------- شبکه روزها ----------
                val cells = firstWeekday + monthLength
                val rows = (cells + 6) / 7
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val index = r * 7 + c
                            val day = index - firstWeekday + 1
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day in 1..monthLength) {
                                    val thisDate = PersianDate(viewYear, viewMonth, day)
                                    val isSelected = thisDate == selected
                                    val isToday = thisDate == today
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) skin.accent else androidx.compose.ui.graphics.Color.Transparent
                                            )
                                            .then(
                                                if (isToday && !isSelected)
                                                    Modifier.border(1.dp, skin.accent, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { selected = thisDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            Digits.toPersian(day.toString()),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) skin.onHero else skin.onBackdrop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    selected = today
                    viewYear = today.year
                    viewMonth = today.month
                }) { Text("امروز") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected) }) { Text("تأیید") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun NavArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val skin = LocalAppSkin.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(skin.accent.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = skin.accent, modifier = Modifier.size(20.dp))
    }
}

/** پنجره انتخاب ساعت با چرخ اسکرولی استوانه‌ای (شبیه انتخابگر آیفون). */
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val skin = LocalAppSkin.current
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = skin.dialogColor,
        title = { Text("انتخاب ساعت") },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                // نوار برجسته وسط که آیتم انتخاب‌شده را قاب می‌گیرد
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(skin.accent.copy(alpha = 0.14f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        range = 0..23,
                        value = hour,
                        onValueChange = { hour = it },
                        modifier = Modifier.width(78.dp)
                    )
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineSmall,
                        color = skin.onBackdrop,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    WheelPicker(
                        range = 0..59,
                        value = minute,
                        onValueChange = { minute = it },
                        modifier = Modifier.width(78.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(hour, minute) }) { Text("تأیید") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

/** ارتفاع هر خانه چرخ بر حسب dp. */
private const val WHEEL_ITEM_HEIGHT = 42

/**
 * چرخ عددی اسکرولی: سه خانه دیده می‌شود، خانه وسط انتخاب است.
 * آیتم‌های دورتر کم‌رنگ و کوچک‌تر می‌شوند تا حس استوانه بدهد،
 * و اسکرول در نزدیک‌ترین خانه قفل (snap) می‌شود.
 */
@Composable
private fun WheelPicker(
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    val items = remember(range) { range.toList() }
    val itemHeight = WHEEL_ITEM_HEIGHT.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (items.indexOf(value)).coerceAtLeast(0)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // اندیس خانه وسط بر اساس موقعیت اسکرول
    val centerIndex by remember {
        derivedStateOf {
            val offsetRows = (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()
            (listState.firstVisibleItemIndex + offsetRows).coerceIn(0, items.lastIndex)
        }
    }

    // هر بار خانه وسط عوض شد، مقدار را به بیرون گزارش می‌کنیم
    LaunchedEffect(centerIndex) {
        items.getOrNull(centerIndex)?.let { if (it != value) onValueChange(it) }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * 3),
        // یک خانه خالی بالا و پایین تا آیتم اول/آخر هم بتواند وسط بایستد
        contentPadding = PaddingValues(vertical = itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items) { index, item ->
            val distance = kotlin.math.abs(index - centerIndex)
            val selected = distance == 0
            val alpha = when (distance) {
                0 -> 1f
                1 -> 0.45f
                else -> 0.2f
            }
            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Digits.toPersian("%02d".format(item)),
                    style = if (selected) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = (if (selected) skin.accent else skin.onBackdrop).copy(alpha = alpha)
                )
            }
        }
    }
}

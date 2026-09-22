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
import androidx.compose.foundation.layout.width
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

/** پنجره انتخاب ساعت با چرخ استوانه‌ای اسکرولی (سبک آیفون). */
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
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // نوار انتخاب وسط چرخ‌ها
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(skin.accent.copy(alpha = 0.14f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        value = hour,
                        range = 0..23,
                        onValueChange = { hour = it },
                        modifier = Modifier.width(72.dp)
                    )
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineSmall,
                        color = skin.onBackdrop,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    WheelPicker(
                        value = minute,
                        range = 0..59,
                        onValueChange = { minute = it },
                        modifier = Modifier.width(72.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(hour, minute) }) { Text("تأیید") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

/**
 * چرخ عددی استوانه‌ای: اعداد بالا و پایین می‌روند، مورد وسط انتخاب می‌شود،
 * و آیتم‌های دورتر کوچک‌تر و کم‌رنگ‌تر می‌شوند تا حس استوانه بدهد.
 * اسکرول با snap روی نزدیک‌ترین آیتم متوقف می‌شود.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    val itemHeight = 40.dp
    val visible = 3 // تعداد آیتم دیده‌شده (وسط + یکی بالا + یکی پایین)
    val count = range.last - range.first + 1

    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = (value - range.first)
    )
    val flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
        lazyListState = listState
    )

    // عددی که در مرکز چرخ قرار گرفته است
    val centered by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            val offsetFraction = listState.firstVisibleItemScrollOffset /
                (itemHeight.value * 2.5f).coerceAtLeast(1f)
            (listState.firstVisibleItemIndex + if (offsetFraction > 0.5f) 1 else 0)
                .coerceIn(0, count - 1)
        }
    }

    androidx.compose.runtime.LaunchedEffect(centered) {
        val v = range.first + centered
        if (v != value) onValueChange(v)
    }

    androidx.compose.foundation.lazy.LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * visible),
        horizontalAlignment = Alignment.CenterHorizontally,
        // فضای خالی بالا و پایین تا اولین و آخرین عدد هم بتوانند وسط بایستند
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = itemHeight)
    ) {
        androidx.compose.foundation.lazy.items(count) { index ->
            val distance = kotlin.math.abs(index - centered)
            val isCenter = distance == 0
            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Digits.toPersian("%02d".format(range.first + index)),
                    style = if (isCenter) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                    color = skin.onBackdrop.copy(
                        // هرچه از مرکز دورتر، کم‌رنگ‌تر — حس عمق استوانه
                        alpha = when (distance) {
                            0 -> 1f
                            1 -> 0.45f
                            else -> 0.2f
                        }
                    )
                )
            }
        }
    }
}

package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableLongStateOf
import ir.kharjyar.app.core.text.Digits
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlin.math.roundToInt

/**
 * کمبوباکس تم‌پذیر: به‌جای ردیف طولانی چیپ‌ها، یک فیلد جمع‌وجور که با کلیک
 * فهرست گزینه‌ها را در یک دیالوگ نشان می‌دهد. نمای برنامه را تمیز نگه می‌دارد.
 */
@Composable
fun <T> ComboBox(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelOf: (T) -> String = { it.toString() },
    leadingOf: (@Composable (T) -> Unit)? = null,
    placeholder: String = "انتخاب کنید"
) {
    val skin = LocalAppSkin.current
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(skin.cardCorner / 1.6f)

    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = skin.onBackdrop.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(skin.cardColor.copy(alpha = if (skin.dark) 0.7f else 1f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .clickable { open = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected != null && leadingOf != null) {
                leadingOf(selected)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = selected?.let(labelOf) ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected != null) skin.onBackdrop else skin.onBackdrop.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = skin.accent)
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = skin.dialogColor,
            titleContentColor = skin.onBackdrop,
            textContentColor = skin.onBackdrop,
            title = { Text(label) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(options.size) { i ->
                        val option = options[i]
                        val isSelected = option == selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(option); open = false }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (leadingOf != null) {
                                leadingOf(option)
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(
                                labelOf(option),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) skin.accent else skin.onBackdrop,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = skin.accent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text("بستن", color = skin.accent) }
            }
        )
    }
}

// ---------------------------------------------------------------- انتخابگر رنگ

/** تبدیل HSV به رنگ. h در ۰..۳۶۰، s و v در ۰..۱. */
internal fun hsvColor(h: Float, s: Float, v: Float): Color =
    Color.hsv(h.coerceIn(0f, 359.999f), s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))

/** استخراج تقریبی HSV از یک رنگ. */
internal fun colorToHsv(color: Color): Triple<Float, Float, Float> {
    val r = color.red; val g = color.green; val b = color.blue
    val max = maxOf(r, g, b); val min = minOf(r, g, b)
    val d = max - min
    val h = when {
        d == 0f -> 0f
        max == r -> (60f * (((g - b) / d) % 6f))
        max == g -> (60f * (((b - r) / d) + 2f))
        else -> (60f * (((r - g) / d) + 4f))
    }.let { if (it < 0f) it + 360f else it }
    val s = if (max == 0f) 0f else d / max
    return Triple(h, s, max)
}

/**
 * انتخابگر رنگ کامل: مربع اشباع/روشنایی + نوار رنگ (hue).
 * جایگزین ردیف محدود دایره‌های از پیش تعریف‌شده.
 */
@Composable
fun ColorPicker(
    color: Long,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    val initial = remember(color) { colorToHsv(Color(color)) }
    var hue by remember { mutableFloatStateOf(initial.first) }
    var sat by remember { mutableFloatStateOf(initial.second) }
    var value by remember { mutableFloatStateOf(initial.third) }

    fun emit() {
        val c = hsvColor(hue, sat, value)
        val argb = (0xFFL shl 24) or
            ((c.red * 255).roundToInt().toLong() shl 16) or
            ((c.green * 255).roundToInt().toLong() shl 8) or
            (c.blue * 255).roundToInt().toLong()
        onColorChange(argb)
    }

    Column(modifier = modifier) {
        // مربع اشباع × روشنایی
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color.White, hsvColor(hue, 1f, 1f))))
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                .pointerInput(hue) {
                    fun update(pos: Offset) {
                        sat = (pos.x / size.width).coerceIn(0f, 1f)
                        value = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                    detectTapGestures { update(it) }
                }
                .pointerInput(hue) {
                    detectDragGestures { change, _ ->
                        sat = (change.position.x / size.width).coerceIn(0f, 1f)
                        value = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                }
        )
        Spacer(Modifier.height(12.dp))

        // نوار رنگ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        (0..6).map { hsvColor(it * 60f, 1f, 1f) }
                    )
                )
                .pointerInput(Unit) {
                    fun update(x: Float) {
                        hue = ((x / size.width).coerceIn(0f, 1f)) * 359.999f
                        emit()
                    }
                    detectTapGestures { update(it.x) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        hue = ((change.position.x / size.width).coerceIn(0f, 1f)) * 359.999f
                        emit()
                    }
                }
        )
        Spacer(Modifier.height(12.dp))

        // پیش‌نمایش + میان‌برهای رایج
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(hsvColor(hue, sat, value))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "رنگ حساب",
                style = MaterialTheme.typography.bodyMedium,
                color = skin.onBackdrop.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            presetColors.forEach { preset ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(preset))
                        .clickable {
                            val hsv = colorToHsv(Color(preset))
                            hue = hsv.first; sat = hsv.second; value = hsv.third
                            onColorChange(preset)
                        }
                )
            }
        }
    }
}

private val presetColors = listOf(
    0xFF3F51B5L, 0xFF00897BL, 0xFFD81B60L, 0xFFF4511EL,
    0xFF7B1FA2L, 0xFF2E7D32L, 0xFF0277BDL, 0xFFE0A33CL
)

/**
 * انتخاب رنگ جمع‌وجور: یک ردیف کوچک که فقط رنگ فعلی را نشان می‌دهد و
 * با لمس، پنجره انتخاب رنگ باز می‌شود. اینطور پالت همیشه جلوی چشم کاربر نیست.
 */
@Composable
fun ColorPickerField(
    color: Long,
    onColorChange: (Long) -> Unit,
    label: String = "رنگ حساب",
    modifier: Modifier = Modifier
) {
    val skin = LocalAppSkin.current
    var open by remember { mutableStateOf(false) }
    var draft by remember(color) { mutableLongStateOf(color) }
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(skin.cardColor.copy(alpha = skin.cardAlpha))
            .border(1.dp, skin.accent.copy(alpha = 0.32f), shape)
            .clickable { draft = color; open = true }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(color))
                .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = skin.onBackdrop)
        Spacer(Modifier.weight(1f))
        Text(
            "تغییر",
            style = MaterialTheme.typography.labelMedium,
            color = skin.accent
        )
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = skin.dialogColor,
            title = { Text("انتخاب رنگ") },
            text = { ColorPicker(color = draft, onColorChange = { draft = it }) },
            confirmButton = {
                TextButton(onClick = { onColorChange(draft); open = false }) { Text("تأیید") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("انصراف") } }
        )
    }
}

/**
 * کمبوباکس قابل جست‌وجو: کاربر چند حرف اول را می‌نویسد و فهرست فیلتر می‌شود.
 * مقدار دلخواه خارج از فهرست هم پذیرفته می‌شود (مثلاً نام بانکی که در لیست نیست).
 */
@Composable
fun SearchableComboBox(
    label: String,
    options: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "برای جست‌وجو تایپ کنید"
) {
    val skin = LocalAppSkin.current
    var open by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }

    val matches = remember(queryText, options) {
        val q = Digits.normalize(queryText).trim()
        if (q.isBlank()) options
        else options.filter { it.contains(q, ignoreCase = true) || it.startsWith(q) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "باز کردن فهرست",
                    tint = skin.accent,
                    modifier = Modifier.clickable { queryText = ""; open = true }
                )
            }
        )
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = skin.dialogColor,
            title = { Text(label) },
            text = {
                Column {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text(placeholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    if (matches.isEmpty()) {
                        Text(
                            "موردی پیدا نشد؛ می‌توانید نام دلخواه را مستقیم بنویسید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = skin.onBackdrop.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(matches.size) { i ->
                                val item = matches[i]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onValueChange(item)
                                            open = false
                                        }
                                        .padding(vertical = 11.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item == value) skin.accent
                                                else skin.onBackdrop.copy(alpha = 0.28f)
                                            )
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        item,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = skin.onBackdrop
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("بستن") } }
        )
    }
}

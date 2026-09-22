package ir.kharjyar.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import ir.kharjyar.app.core.text.Digits
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

/**
 * ابزار ورودی مبلغ: فقط رقم می‌پذیرد، حین تایپ سه‌رقمی جدا می‌کند و ارقام را فارسی نشان می‌دهد.
 */
object AmountInput {

    /** فقط رقم‌های لاتین را از ورودی (فارسی/عربی/لاتین، با هر جداکننده‌ای) بیرون می‌کشد. */
    fun sanitize(raw: String, maxDigits: Int = 15): String {
        val digits = Digits.normalize(raw).filter { it in '0'..'9' }
        if (digits.isEmpty()) return ""
        val trimmed = digits.trimStart('0')
        return (if (trimmed.isEmpty()) "0" else trimmed).take(maxDigits)
    }

    /** «۱۲۳۴۵۶» → «۱۲۳،۴۵۶» (ارقام فارسی، جداکننده «،»). */
    fun grouped(digits: String): String {
        if (digits.isEmpty()) return ""
        return Digits.toPersian(digits.reversed().chunked(3).joinToString("،").reversed())
    }
}

/**
 * VisualTransformation جداکننده هزارگان با «،» و ارقام فارسی.
 * ورودی اصلی باید فقط ارقام لاتین باشد (خروجی [AmountInput.sanitize]).
 */
class ThousandsSeparatorTransformation(
    private val persianDigits: Boolean = true
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        val out = StringBuilder()
        // نگاشت: به ازای هر موقعیت اصلی (۰..n) موقعیت متناظر در متن نمایشی
        val originalToOut = IntArray(digits.length + 1)
        digits.forEachIndexed { i, c ->
            originalToOut[i] = out.length
            out.append(if (persianDigits) Digits.toPersian(c.toString()) else c.toString())
            val remaining = digits.length - i - 1
            if (remaining > 0 && remaining % 3 == 0) out.append('،')
        }
        originalToOut[digits.length] = out.length

        val rendered = out.toString()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToOut[offset.coerceIn(0, digits.length)]

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, rendered.length)
                // نزدیک‌ترین موقعیت اصلی که نگاشتش از o بیشتر نشود
                var result = 0
                for (i in 0..digits.length) {
                    if (originalToOut[i] <= o) result = i else break
                }
                return result
            }
        }
        return TransformedText(AnnotatedString(rendered), mapping)
    }
}

/**
 * فیلد مبلغ استاندارد برنامه:
 * - کیبورد عددی پایدار (KeyboardType.Number) که پس از هر رقم به حروف برنمی‌گردد
 * - جداکننده هزارگان خودکار حین تایپ
 * - bringIntoViewRequester تا هنگام باز شدن کیبورد فیلد زیر آن پنهان نشود
 *
 * [value] و [onValueChange] روی رشته ارقام خام (لاتین، بدون جداکننده) کار می‌کنند.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(AmountInput.sanitize(it)) },
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = ThousandsSeparatorTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = modifier
            .bringIntoViewRequester(requester)
            .onFocusEvent { state ->
                if (state.isFocused) scope.launch { requester.bringIntoView() }
            }
    )
}

/**
 * فیلد عددی ساده (تاریخ/ساعت/دقیقه): کیبورد عددی + آوردن به دید هنگام فوکوس.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    maxDigits: Int = 4,
    imeAction: ImeAction = ImeAction.Next
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = Digits.toPersian(value),
        onValueChange = { raw ->
            onValueChange(Digits.normalize(raw).filter { it in '0'..'9' }.take(maxDigits))
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = modifier
            .bringIntoViewRequester(requester)
            .onFocusEvent { state ->
                if (state.isFocused) scope.launch { requester.bringIntoView() }
            }
    )
}

/** Modifier کمکی: فیلد متنی معمولی را هم هنگام فوکوس بالای کیبورد نگه می‌دارد. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.keepAboveKeyboard(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val scope: CoroutineScope = rememberCoroutineScope()
    return this
        .bringIntoViewRequester(requester)
        .onFocusEvent { state -> if (state.isFocused) scope.launch { requester.bringIntoView() } }
}

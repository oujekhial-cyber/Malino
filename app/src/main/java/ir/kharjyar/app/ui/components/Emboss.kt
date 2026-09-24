package ir.kharjyar.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * متن «برجسته» (اِمباس) برای کارت اصلی صفحه خانه.
 *
 * سه لایه روی هم رسم می‌شود: یک سایه تیره کمی پایین‌تر، یک روشنایی کمی بالاتر
 * و متن اصلی در وسط. نتیجه حس حکاکی/برجستگی می‌دهد بدون اینکه خوانایی کم شود.
 */
@Composable
fun EmbossedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    depth: Float = 1f
) {
    val shadow = Color.Black.copy(alpha = 0.45f * depth)
    val light = Color.White.copy(alpha = 0.34f * depth)
    val glow = style.copy(
        shadow = Shadow(color = Color.Black.copy(alpha = 0.30f * depth), offset = Offset(0f, 2f), blurRadius = 8f)
    )
    Box(modifier) {
        Text(
            text,
            style = style,
            color = shadow,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 0.8.dp, y = 1.2.dp)
        )
        Text(
            text,
            style = style,
            color = light,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = (-0.8).dp, y = (-1.2).dp)
        )
        Text(
            text,
            style = glow,
            color = color,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

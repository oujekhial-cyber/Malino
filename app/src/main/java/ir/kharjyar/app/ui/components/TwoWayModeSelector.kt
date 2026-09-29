package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** انتخاب‌گر برجستهٔ دوحالته برای نوع سند، همیشه در ابتدای صفحه. */
@Composable
fun TwoWayModeSelector(
    first: String,
    second: String,
    firstSelected: Boolean,
    firstColor: Color,
    secondColor: Color,
    onFirst: () -> Unit,
    onSecond: () -> Unit
) {
    val outer = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().clip(outer)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        fun Modifier.mode(selected:Boolean,color:Color,onClick:()->Unit)=weight(1f).clip(RoundedCornerShape(14.dp))
            .background(if(selected) color else Color.Transparent).clickable(onClick=onClick).padding(vertical=12.dp)
        Box(Modifier.mode(firstSelected,firstColor,onFirst), contentAlignment=Alignment.Center) {
            Text(first,color=if(firstSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=FontWeight.Bold)
        }
        Box(Modifier.mode(!firstSelected,secondColor,onSecond), contentAlignment=Alignment.Center) {
            Text(second,color=if(!firstSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=FontWeight.Bold)
        }
    }
}

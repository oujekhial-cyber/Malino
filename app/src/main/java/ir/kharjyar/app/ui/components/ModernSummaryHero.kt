package ir.kharjyar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SummaryMetric(val label:String,val value:String,val color:Color,val icon:ImageVector)

/** سربرگ گرافیکی مشترک برای صفحات مدیریتی؛ خوانا در تمام تم‌های روشن و تیره. */
@Composable fun ModernSummaryHero(title:String,subtitle:String,accent:Color,metrics:List<SummaryMetric>,icon:ImageVector=Icons.Filled.AutoGraph){
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),border=BorderStroke(1.dp,accent.copy(.32f)),shadowElevation=5.dp){
  Box(Modifier.background(Brush.linearGradient(listOf(accent.copy(.15f),Color.Transparent,accent.copy(.06f)))).padding(15.dp)){
   Column(verticalArrangement=Arrangement.spacedBy(13.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(43.dp).background(accent.copy(.14f),RoundedCornerShape(14.dp)).border(1.dp,accent.copy(.42f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(icon,null,tint=accent,modifier=Modifier.size(24.dp))};Spacer(Modifier.width(10.dp));Column{Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){metrics.forEach{m->Surface(Modifier.weight(1f),shape=RoundedCornerShape(16.dp),color=m.color.copy(.09f),border=BorderStroke(1.dp,m.color.copy(.25f))){Row(Modifier.padding(horizontal=10.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(32.dp).background(m.color.copy(.15f),CircleShape),contentAlignment=Alignment.Center){Icon(m.icon,null,tint=m.color,modifier=Modifier.size(17.dp))};Spacer(Modifier.width(7.dp));Column{Text(m.value,fontWeight=FontWeight.Black,color=m.color,style=MaterialTheme.typography.titleMedium,maxLines=1);Text(m.label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)}}}}
   }
  }
 }
}

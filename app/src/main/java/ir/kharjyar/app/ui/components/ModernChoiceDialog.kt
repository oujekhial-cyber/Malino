package ir.kharjyar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ModernChoiceOption(val title:String,val subtitle:String,val color:Color,val icon:ImageVector,val enabled:Boolean=true,val onClick:()->Unit)

@Composable fun ModernChoiceDialog(title:String,subtitle:String,options:List<ModernChoiceOption>,onDismiss:()->Unit){
 AlertDialog(
  onDismissRequest=onDismiss,
  shape=RoundedCornerShape(28.dp),
  containerColor=MaterialTheme.colorScheme.surface,
  title={Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary,MaterialTheme.colorScheme.tertiary)),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(Icons.Filled.AutoAwesome,null,tint=Color.White)};Spacer(Modifier.width(10.dp));Text(title,fontWeight=FontWeight.Black)};Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}},
  text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){options.forEach{item->Surface(Modifier.fillMaxWidth().clickable(enabled=item.enabled){item.onClick()},shape=RoundedCornerShape(20.dp),color=item.color.copy(alpha=if(item.enabled).11f else .04f),border=BorderStroke(1.2.dp,item.color.copy(alpha=if(item.enabled).42f else .12f))){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).background(item.color.copy(.16f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Icon(item.icon,null,tint=if(item.enabled)item.color else MaterialTheme.colorScheme.outline)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(item.title,fontWeight=FontWeight.Bold,color=if(item.enabled)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline);Text(item.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("←",color=item.color,style=MaterialTheme.typography.titleLarge)}}}},
  confirmButton={},dismissButton={TextButton(onDismiss){Text("انصراف")}}
 )
}

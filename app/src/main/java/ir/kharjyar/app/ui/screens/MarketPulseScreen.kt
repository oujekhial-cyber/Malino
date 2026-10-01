package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.assets.GoldPriceService
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MarketItem(val code:String,val title:String,val subtitle:String,val symbol:String,val accent:Color)
private val metalItems=listOf(
 MarketItem("GOLD18","طلای ۱۸ عیار","هر گرم","Au",Color(0xFFFFC54D)),
 MarketItem("GOLD24","طلای ۲۴ عیار","هر گرم","24",Color(0xFFFFA83D)),
 MarketItem("MESGHAL","مثقال طلا","بازار ایران","مث",Color(0xFFE59E31)),
 MarketItem("SILVER","نقره ۹۹۹","هر گرم","Ag",Color(0xFFB8C5D6)))
private val currencyItems=listOf(
 MarketItem("USD","دلار آمریکا","بازار آزاد","\$",Color(0xFF35C4BB)),MarketItem("EUR","یورو","بازار آزاد","€",Color(0xFF5798FF)),
 MarketItem("GBP","پوند انگلیس","بازار آزاد","£",Color(0xFFAC79F2)),MarketItem("AED","درهم امارات","بازار آزاد","د.إ",Color(0xFF43B985)),
 MarketItem("TRY","لیر ترکیه","بازار آزاد","₺",Color(0xFFE98563)),MarketItem("CAD","دلار کانادا","بازار آزاد","C\$",Color(0xFFE45D79)),
 MarketItem("AUD","دلار استرالیا","بازار آزاد","A\$",Color(0xFF49A5D8)),MarketItem("CHF","فرانک سوئیس","بازار آزاد","Fr",Color(0xFFDF6B70)),
 MarketItem("CNY","یوان چین","بازار آزاد","¥",Color(0xFFE75858)))

@Composable fun MarketPulseScreen(vm:AppViewModel){
 val settings by vm.settings.collectAsState();var refreshKey by remember{mutableStateOf(0)};var loading by remember{mutableStateOf(true)};var values by remember{mutableStateOf<Map<String,Long?>>(emptyMap())};var updatedAt by remember{mutableStateOf("—")}
 LaunchedEffect(refreshKey){loading=true;values=coroutineScope{(metalItems+currencyItems).associate{item->item.code to async{if(item in metalItems)GoldPriceService.preciousMetalRial(item.code) else GoldPriceService.currencyRial(item.code)}}.mapValues{it.value.await()}};updatedAt=Digits.toPersian(SimpleDateFormat("HH:mm",Locale.US).format(Date()));loading=false}
 val bg=Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.primary.copy(.055f),MaterialTheme.colorScheme.background))
 Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(.72f))){Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0x22FF3F9A),Color.Transparent,Color(0x2247E4DC)))).padding(18.dp)){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF39C7C0).copy(.14f)).border(1.dp,Color(0xFF39C7C0).copy(.55f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Icon(Icons.Filled.AutoGraph,null,tint=Color(0xFF39C7C0),modifier=Modifier.size(30.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("نبض بازار",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("قیمت آنلاین بازار ایران",color=MaterialTheme.colorScheme.onSurfaceVariant)};FilledIconButton({refreshKey++},enabled=!loading,colors=IconButtonDefaults.filledIconButtonColors(containerColor=MaterialTheme.colorScheme.primary.copy(.14f))){Icon(Icons.Filled.Refresh,"بروزرسانی")}};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth());Text("آخرین دریافت: امروز، $updatedAt  •  منبع عمومی TGJU",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("قیمت‌ها صرفاً جهت اطلاع‌اند؛ مقدار ناموجود تخمین زده نمی‌شود.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
  MarketSection("فلزات گران‌بها","طلا، مثقال و نقره",Icons.Filled.WorkspacePremium,metalItems,values,settings.moneyUnit)
  MarketSection("ارزهای رایج","نرخ آزاد ارز در بازار ایران",Icons.Filled.CurrencyExchange,currencyItems,values,settings.moneyUnit)
  Spacer(Modifier.height(20.dp))
 }
}

@Composable private fun MarketSection(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,items:List<MarketItem>,values:Map<String,Long?>,unit:ir.kharjyar.app.core.money.MoneyUnit){
 Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Column{Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}};items.chunked(2).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{item->MarketPriceCard(item,values[item.code],unit,Modifier.weight(1f))};if(row.size==1)Spacer(Modifier.weight(1f))}}}
}

@Composable private fun MarketPriceCard(item:MarketItem,value:Long?,unit:ir.kharjyar.app.core.money.MoneyUnit,modifier:Modifier){Card(modifier,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(.88f)),border=androidx.compose.foundation.BorderStroke(1.dp,item.accent.copy(.42f))){Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(item.accent.copy(.10f),Color.Transparent))).padding(13.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)){Box(Modifier.size(42.dp).background(item.accent.copy(.14f),CircleShape).border(1.3.dp,item.accent.copy(.7f),CircleShape),contentAlignment=Alignment.Center){Text(item.symbol,color=item.accent,fontWeight=FontWeight.Black)};Text(item.title,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,maxLines=1);Text(item.subtitle,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);HorizontalDivider(color=item.accent.copy(.2f));Text(value?.let{Money.format(it,unit)}?:"دریافت نشد",color=if(value!=null)item.accent else MaterialTheme.colorScheme.error,fontWeight=FontWeight.Black,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center,maxLines=1)}}}

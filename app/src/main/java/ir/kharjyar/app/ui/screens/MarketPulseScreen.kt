package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.assets.GoldPriceService
import ir.kharjyar.app.assets.MarketPriceCache
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private data class MarketItem(val code:String,val title:String,val subtitle:String,val symbol:String,val accent:Color)
private val metalItems=listOf(
 MarketItem("GOLD18","طلای ۱۸ عیار","هر گرم","Au",Color(0xFFFFC54D)),
 MarketItem("GOLD24","طلای ۲۴ عیار","هر گرم","24",Color(0xFFFFA83D)),
 MarketItem("MESGHAL","مثقال طلا","بازار ایران","مث",Color(0xFFE59E31)),
 MarketItem("SILVER","نقره ۹۹۹","هر گرم","Ag",Color(0xFF9EACBF)))
private val currencyItems=listOf(
 MarketItem("USD","دلار آمریکا","بازار آزاد","\$",Color(0xFF35C4BB)),MarketItem("EUR","یورو","بازار آزاد","€",Color(0xFF5798FF)),
 MarketItem("GBP","پوند انگلیس","بازار آزاد","£",Color(0xFFAC79F2)),MarketItem("AED","درهم امارات","بازار آزاد","د.إ",Color(0xFF43B985)),
 MarketItem("TRY","لیر ترکیه","بازار آزاد","₺",Color(0xFFE98563)),MarketItem("CAD","دلار کانادا","بازار آزاد","C\$",Color(0xFFE45D79)),
 MarketItem("AUD","دلار استرالیا","بازار آزاد","A\$",Color(0xFF49A5D8)),MarketItem("CHF","فرانک سوئیس","بازار آزاد","Fr",Color(0xFFDF6B70)),
 MarketItem("CNY","یوان چین","بازار آزاد","¥",Color(0xFFE75858)))

@Composable fun MarketPulseScreen(vm:AppViewModel){
 val settings by vm.settings.collectAsState();val context=LocalContext.current;val allItems=metalItems+currencyItems
 var refreshKey by remember{mutableStateOf(0)};var loading by remember{mutableStateOf(true)}
 var values by remember{mutableStateOf<Map<String,Long?>>(emptyMap())};var previousValues by remember{mutableStateOf<Map<String,Long?>>(emptyMap())}
 var updatedAt by remember{mutableStateOf<Long?>(null)};var stale by remember{mutableStateOf(false)}
 LaunchedEffect(refreshKey){
  loading=true;val cached=allItems.associate{it.code to MarketPriceCache.read(context,it.code)}
  if(values.isEmpty())values=cached.mapValues{it.value?.valueRial}
  val fresh=coroutineScope{allItems.associate{item->item.code to async{if(item in metalItems)GoldPriceService.preciousMetalRial(item.code) else GoldPriceService.currencyRial(item.code)}}.mapValues{it.value.await()}}
  val now=System.currentTimeMillis();if(values.values.any{it!=null})previousValues=values
  fresh.forEach{(code,value)->if(value!=null)MarketPriceCache.write(context,code,value,now)}
  stale=fresh.values.any{it==null};values=fresh.mapValues{(code,value)->value?:cached[code]?.valueRial}
  updatedAt=if(stale)fresh.mapNotNull{(code,value)->if(value==null)cached[code]?.updatedAt else now}.minOrNull() else now;loading=false
 }
 val bg=Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background,MaterialTheme.colorScheme.primary.copy(.045f),MaterialTheme.colorScheme.background))
 LazyColumn(Modifier.fillMaxSize().background(bg),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{MarketHeader(updatedAt,loading,stale){refreshKey++}}
  item{MarketSectionHeader("فلزات گران‌بها","طلا، مثقال و نقره",Icons.Filled.WorkspacePremium)}
  item{MarketList(metalItems,values,previousValues,settings.moneyUnit)}
  item{MarketSectionHeader("ارزهای رایج","نرخ آزاد بازار ایران",Icons.Filled.CurrencyExchange)}
  item{MarketList(currencyItems,values,previousValues,settings.moneyUnit)}
  item{Text("قیمت‌ها صرفاً جهت اطلاع‌اند و مقدار ناموجود تخمین زده نمی‌شود.",Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=6.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)}
 }
}

@Composable private fun MarketHeader(updatedAt:Long?,loading:Boolean,stale:Boolean,onRefresh:()->Unit){
 Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(.72f)),border=BorderStroke(1.dp,MaterialTheme.colorScheme.primary.copy(.18f))){
  Box(Modifier.background(Brush.horizontalGradient(listOf(Color(0x1735C4BB),Color.Transparent,Color(0x16FF4C9A)))).padding(15.dp)){
   Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(Color(0xFF35C4BB).copy(.14f),RoundedCornerShape(14.dp)).border(1.dp,Color(0xFF35C4BB).copy(.48f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(Icons.Filled.AutoGraph,null,tint=Color(0xFF35C4BB))};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text("نمای کلی بازار",fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleLarge);Text("آخرین بروزرسانی: ${updatedAt?.let{PersianDate.formatDateTime(it)}?:"—"}",style=MaterialTheme.typography.bodySmall,color=if(stale)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)};FilledIconButton(onRefresh,enabled=!loading,colors=IconButtonDefaults.filledIconButtonColors(containerColor=MaterialTheme.colorScheme.primary.copy(.13f))){Icon(Icons.Filled.Refresh,"به‌روزرسانی")}};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth()) else Row(verticalAlignment=Alignment.CenterVertically){Icon(if(stale)Icons.Filled.CloudOff else Icons.Filled.Verified,null,Modifier.size(15.dp),tint=if(stale)MaterialTheme.colorScheme.error else Color(0xFF35B98B));Spacer(Modifier.width(5.dp));Text(if(stale)"اتصال ممکن نشد؛ آخرین قیمت ذخیره‌شده" else "داده عمومی TGJU",style=MaterialTheme.typography.labelSmall,color=if(stale)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)}}
  }
 }
}

@Composable private fun MarketSectionHeader(title:String,subtitle:String,icon:ImageVector){Row(Modifier.fillMaxWidth().padding(horizontal=4.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(23.dp));Spacer(Modifier.width(8.dp));Column{Text(title,fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleMedium);Text(subtitle,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}

@Composable private fun MarketList(items:List<MarketItem>,values:Map<String,Long?>,previous:Map<String,Long?>,unit:MoneyUnit){
 Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(.90f)),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(.65f))){Column{items.forEachIndexed{i,item->MarketRow(item,values[item.code],previous[item.code],unit);if(i<items.lastIndex)HorizontalDivider(Modifier.padding(start=68.dp),color=MaterialTheme.colorScheme.outlineVariant.copy(.55f))}}}
}

@Composable private fun MarketRow(item:MarketItem,value:Long?,previous:Long?,unit:MoneyUnit){
 val direction=if(value!=null&&previous!=null) value.compareTo(previous) else 0
 Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(item.accent.copy(.055f),Color.Transparent))).padding(horizontal=13.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){
  Box(Modifier.size(43.dp).background(item.accent.copy(.13f),CircleShape).border(1.dp,item.accent.copy(.55f),CircleShape),contentAlignment=Alignment.Center){Text(item.symbol,color=item.accent,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,style=MaterialTheme.typography.labelLarge)}
  Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(item.title,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodyLarge);Text(item.subtitle,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  Column(horizontalAlignment=Alignment.End,verticalArrangement=Arrangement.spacedBy(3.dp)){Text(value?.let{Money.format(it,unit)}?:"دریافت نشد",fontWeight=FontWeight.Black,color=if(value==null)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,maxLines=1);if(direction!=0){val up=direction>0;Row(verticalAlignment=Alignment.CenterVertically){Icon(if(up)Icons.Filled.NorthEast else Icons.Filled.SouthWest,null,Modifier.size(14.dp),tint=if(up)Color(0xFF22A878) else Color(0xFFE05C68));Spacer(Modifier.width(2.dp));Text(if(up)"افزایش" else "کاهش",style=MaterialTheme.typography.labelSmall,color=if(up)Color(0xFF22A878) else Color(0xFFE05C68))}}else Text(if(value!=null&&previous!=null)"بدون تغییر" else " ",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

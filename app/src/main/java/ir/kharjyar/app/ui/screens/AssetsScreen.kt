package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.assets.GoldPriceService
import ir.kharjyar.app.assets.IranianGoldCalculator
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.SwipeActionRow
import kotlinx.coroutines.launch

@Composable fun AssetsScreen(vm:AppViewModel){val assets by vm.assets.collectAsState();val settings by vm.settings.collectAsState();val scope=rememberCoroutineScope();var editor by remember{mutableStateOf<AssetEntity?>(null)};var adding by remember{mutableStateOf(false)};var pendingDelete by remember{mutableStateOf<AssetEntity?>(null)}
 BackHandler(enabled=adding||editor!=null){adding=false;editor=null}
 if(adding||editor!=null){AssetEditor(vm,editor,{adding=false;editor=null},{adding=false;editor=null});return}
 val active=assets.filter{it.active};val totalBuy=active.sumOf{it.purchasePriceRial};val totalNow=active.sumOf{it.currentValueRial};val profit=totalNow-totalBuy
 Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("سبد دارایی‌ها",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Card(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=.88f),MaterialTheme.colorScheme.tertiary.copy(alpha=.72f)))).padding(20.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text("ارزش روز کل دارایی‌ها",color=Color.White.copy(.85f));Text(Money.format(totalNow,settings.moneyUnit),color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);HorizontalDivider(color=Color.White.copy(.25f));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("سرمایه اولیه",color=Color.White.copy(.8f));Text(Money.format(totalBuy,settings.moneyUnit),color=Color.White)};Column(horizontalAlignment=Alignment.End){Text(if(profit>=0)"سود کل" else "زیان کل",color=Color.White.copy(.8f));Text(Money.format(kotlin.math.abs(profit),settings.moneyUnit),color=Color.White,fontWeight=FontWeight.Bold)}}}}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){MiniAssetStat("دارایی فعال",active.size.toString(),MaterialTheme.colorScheme.primary);MiniAssetStat("طلا",active.count{it.kind==AssetKind.GOLD}.toString(),Color(0xFFFFB300));MiniAssetStat("ملک و خودرو",active.count{it.kind==AssetKind.PROPERTY||it.kind==AssetKind.VEHICLE}.toString(),Color(0xFF3F7DE0))}
  Text("دارایی‌های من",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(active.isEmpty())Card(Modifier.fillMaxWidth()){Column(Modifier.padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("هنوز دارایی ثبت نشده است");Text("با دکمه + اولین دارایی را اضافه کنید",style=MaterialTheme.typography.bodySmall)}}
  active.forEach{asset->SwipeActionRow(onDelete={pendingDelete=asset},onEdit={editor=asset}){AssetGraphicCard(asset,settings.moneyUnit,onUpdate={value->scope.launch{vm.repo.db.assetDao().update(asset.copy(currentValueRial=value))}},onSale={scope.launch{vm.repo.db.assetDao().insertTrade(AssetTradeEntity(assetId=asset.id,isSale=true,quantity=asset.quantity,amountRial=asset.currentValueRial,tradedAt=System.currentTimeMillis()));vm.repo.db.assetDao().update(asset.copy(active=false))}})}}
 }
 FloatingActionButton({adding=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن دارایی")}}
 pendingDelete?.let{asset->AlertDialog(onDismissRequest={pendingDelete=null},title={Text("حذف دارایی")},text={Text("آیا «${asset.title}» و سوابق خرید و فروش آن حذف شود؟")},confirmButton={TextButton({pendingDelete=null;scope.launch{vm.repo.db.assetDao().deleteTrades(asset.id);vm.repo.db.assetDao().delete(asset.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton({pendingDelete=null}){Text("انصراف")}})}
}

@Composable private fun MiniAssetStat(label:String,value:String,color:Color){Card(shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(horizontal=14.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(value),color=color,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.labelSmall)}}}

@Composable private fun AssetGraphicCard(asset:AssetEntity,unit:ir.kharjyar.app.core.money.MoneyUnit,onUpdate:(Long)->Unit,onSale:()->Unit){val accent=when(asset.kind){AssetKind.GOLD->Color(0xFFFFB300);AssetKind.VEHICLE->Color(0xFF3185D8);AssetKind.PROPERTY->Color(0xFF8C5BC2);else->Color(0xFF26A69A)};val icon=when(asset.kind){AssetKind.GOLD->"ط";AssetKind.VEHICLE->"خ";AssetKind.PROPERTY->"م";else->"د"};val gain=asset.currentValueRial-asset.purchasePriceRial;var edited by remember(asset.id,asset.currentValueRial){mutableStateOf(asset.currentValueRial.toString())}
 Card(shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.72f))){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(accent,accent.copy(.55f))),CircleShape),contentAlignment=Alignment.Center){Text(icon,color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(asset.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(asset.note.ifBlank{when(asset.kind){AssetKind.GOLD->"طلا";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"ملک";else->"دارایی شخصی"}},color=MaterialTheme.colorScheme.onSurfaceVariant)};Column(horizontalAlignment=Alignment.End){Text("ارزش روز",style=MaterialTheme.typography.labelSmall);Text(Money.format(asset.currentValueRial,unit),fontWeight=FontWeight.Bold,color=accent)}};HorizontalDivider();Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("خرید ${PersianDate.fromMillis(asset.purchasedAt).format()}: ${Money.format(asset.purchasePriceRial,unit)}",style=MaterialTheme.typography.bodySmall);Text((if(gain>=0)"▲ " else "▼ ")+Money.format(kotlin.math.abs(gain),unit),color=if(gain>=0)Color(0xFF20A565) else Color(0xFFE14B55),fontWeight=FontWeight.Bold)};AmountTextField(edited,{edited=it},"بروزرسانی ارزش روز");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({onUpdate(Digits.parseAmount(edited)?:asset.currentValueRial)},Modifier.weight(1f)){Text("ذخیره ارزش")};OutlinedButton(onSale,Modifier.weight(1f)){Text("ثبت فروش")}};Text("برای ویرایش به راست و برای حذف به چپ بکشید",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
}

@Composable
private fun AssetEditor(vm:AppViewModel,existing:AssetEntity?,onDone:()->Unit,onCancel:()->Unit){

 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val settings by vm.settings.collectAsState()
 var kind by remember{mutableStateOf(existing?.kind?:AssetKind.GOLD)}
 var title by remember{mutableStateOf(existing?.title.orEmpty())}
 var quantity by remember{mutableStateOf(existing?.quantity?.toString() ?: "1")}
 var purchaseDate by remember(existing?.id){mutableStateOf(existing?.let{PersianDate.fromMillis(it.purchasedAt)}?:PersianDate.today())}
 var purchase by remember{mutableStateOf(existing?.purchasePriceRial?.toString().orEmpty())}
 var current by remember{mutableStateOf(existing?.currentValueRial?.toString().orEmpty())}
 val kinds=listOf(AssetKind.GOLD,AssetKind.VEHICLE,AssetKind.PROPERTY,AssetKind.OTHER)
 val goldTypes=listOf("طلای آب‌شده","طلای شکسته/دست‌دوم","طلای نو")
 var goldType by remember{mutableStateOf(existing?.note?.takeIf{it in goldTypes}?:goldTypes.first())}
 var gramPrice by remember{mutableStateOf("")}
 var lookup by remember{mutableStateOf<String?>(null)}

 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
  TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به سبد دارایی")}
  Text(if(existing==null)"افزودن دارایی" else "ویرایش دارایی",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  ComboBox("نوع دارایی",kinds,kind,{if(existing==null)kind=it},labelOf={when(it){AssetKind.GOLD->"طلا";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"ملک";else->"دارایی شخصی"}})
  OutlinedTextField(title,{title=it},label={Text("عنوان دارایی")},modifier=Modifier.fillMaxWidth())
  if(kind==AssetKind.GOLD){
   ComboBox("نوع طلا",goldTypes,goldType,{goldType=it},labelOf={it})
   OutlinedTextField(quantity,{quantity=Digits.normalize(it)},label={Text("وزن (گرم)")},modifier=Modifier.fillMaxWidth())
  }else OutlinedTextField(quantity,{quantity=Digits.normalize(it)},label={Text("تعداد")},modifier=Modifier.fillMaxWidth())

  HorizontalDivider()
  Text("اطلاعات خرید",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
  Text("تاریخ و مبلغ واقعی خرید را مطابق همان زمان، به‌صورت دستی وارد کنید. این مبلغ با استعلام روز تغییر نمی‌کند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  PersianDateField(purchaseDate,{purchaseDate=it})
  AmountTextField(purchase,{purchase=it},"قیمت خرید کل در تاریخ خرید (ریال)",Modifier.fillMaxWidth())

  HorizontalDivider()
  Text("ارزش امروز",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
  if(kind==AssetKind.GOLD){
   OutlinedButton({scope.launch{
    val value=GoldPriceService.gram18Rial()
    if(value!=null){gramPrice=value.toString();lookup="نرخ روز طلای ۱۸ عیار ایران دریافت شد"}
    else lookup="اتصال برقرار نشد؛ نرخ روز را دستی وارد کنید"
   }},Modifier.fillMaxWidth()){Text("استعلام نرخ روز طلای ۱۸ عیار ایران")}
   AmountTextField(gramPrice,{gramPrice=it},"نرخ روز هر گرم طلای ۱۸ عیار (ریال)",Modifier.fillMaxWidth())
   lookup?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
   Button({
    val weight=quantity.toDoubleOrNull()?:0.0
    val rate=Digits.parseAmount(gramPrice)?:0
    current=IranianGoldCalculator.rawOrUsed(weight,rate).toString()
   },Modifier.fillMaxWidth(),enabled=(quantity.toDoubleOrNull()?:0.0)>0&&(Digits.parseAmount(gramPrice)?:0)>0){Text("محاسبه ارزش روز طلا")}
   Text("ارزش روز بر اساس وزن و نرخ روز محاسبه می‌شود؛ قیمت تاریخی خرید بدون تغییر باقی می‌ماند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }else{
   Text(if(kind==AssetKind.VEHICLE)"قیمت روز خودرو را کاربر وارد می‌کند." else if(kind==AssetKind.PROPERTY)"قیمت روز ملک را کاربر وارد می‌کند." else "ارزش روز دارایی را وارد کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  AmountTextField(current,{current=it},"ارزش روز کل (ریال)",Modifier.fillMaxWidth())
  val purchaseValue=Digits.parseAmount(purchase)?:0
  val currentValue=Digits.parseAmount(current)?:0
  if(purchaseValue>0&&currentValue>0){
   val difference=currentValue-purchaseValue
   Card(colors=CardDefaults.cardColors(containerColor=(if(difference>=0)Color(0xFF20A565) else Color(0xFFE14B55)).copy(alpha=.10f)),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(12.dp)){Text(if(difference>=0)"سود فعلی" else "زیان فعلی",fontWeight=FontWeight.Bold,color=if(difference>=0)Color(0xFF20A565) else Color(0xFFE14B55));Text(Money.format(kotlin.math.abs(difference),settings.moneyUnit));Text("نسبت تغییر: ${String.format(java.util.Locale.US,"%.1f",difference.toDouble()/purchaseValue*100)}٪")}
   }
  }
  Button({scope.launch{
   val value=AssetEntity(id=existing?.id?:0,kind=kind,title=title.trim(),quantity=quantity.toDoubleOrNull()?:1.0,purchasePriceRial=purchaseValue,currentValueRial=currentValue,purchasedAt=purchaseDate.startOfDayMillis(),note=if(kind==AssetKind.GOLD)goldType else existing?.note.orEmpty(),active=existing?.active?:true)
   if(existing==null)vm.repo.db.assetDao().insert(value)else vm.repo.db.assetDao().update(value)
   showSavedMessage(context,"دارایی")
   onDone()
  }},enabled=title.isNotBlank()&&purchaseValue>0&&currentValue>0,modifier=Modifier.fillMaxWidth()){Text(if(existing==null)"ثبت دارایی" else "ذخیره تغییرات")}
 }
}

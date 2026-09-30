package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.focus.onFocusChanged
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

@Composable fun AssetsScreen(vm:AppViewModel){val assets by vm.assets.collectAsState();val trades by vm.assetTrades.collectAsState();val settings by vm.settings.collectAsState();val stockDrafts by remember{vm.repo.db.stockDao().observePending()}.collectAsState(initial=emptyList());val scope=rememberCoroutineScope();var editor by remember{mutableStateOf<AssetEntity?>(null)};var adding by remember{mutableStateOf(false)};var selectedKind by remember{mutableStateOf<Int?>(null)};var purchaseAsset by remember{mutableStateOf<AssetEntity?>(null)};var pendingDelete by remember{mutableStateOf<AssetEntity?>(null)}
 fun confirmStockDraft(draft:StockSmsDraftEntity){scope.launch{val dao=vm.repo.db.assetDao();val key="STOCK|${draft.broker}|${draft.symbol}|";val old=dao.allAssetsOnce().firstOrNull{it.kind==AssetKind.STOCK&&it.note.startsWith(key)&&it.active};if(draft.side==StockSide.BUY){val oldQty=old?.quantity?:0.0;val newQty=oldQty+draft.quantity;val newCost=(old?.purchasePriceRial?:0L)+draft.totalRial;val value=AssetEntity(id=old?.id?:0,kind=AssetKind.STOCK,title=draft.symbol,quantity=newQty,purchasePriceRial=newCost,currentValueRial=(newQty*draft.unitPriceRial).toLong(),purchasedAt=old?.purchasedAt?:draft.occurredAt,note="$key${(newCost/newQty).toLong()}");val id=if(old==null)dao.insert(value)else{dao.update(value);old.id};dao.insertTrade(AssetTradeEntity(assetId=id,isSale=false,quantity=draft.quantity.toDouble(),amountRial=draft.totalRial,tradedAt=draft.occurredAt))}else{if(old==null||old.quantity<draft.quantity)return@launch;val sold=draft.quantity.toDouble();val remain=old.quantity-sold;val avg=if(old.quantity>0)old.purchasePriceRial/old.quantity else 0.0;dao.insertTrade(AssetTradeEntity(assetId=old.id,isSale=true,quantity=sold,amountRial=draft.totalRial,tradedAt=draft.occurredAt));dao.update(old.copy(quantity=remain,purchasePriceRial=(avg*remain).toLong(),currentValueRial=(if(old.quantity>0)old.currentValueRial/old.quantity*remain else 0.0).toLong(),active=remain>0))};vm.repo.db.stockDao().updateDraft(draft.copy(status=StockDraftStatus.CONFIRMED))}}
 BackHandler(enabled=adding||editor!=null||purchaseAsset!=null||selectedKind!=null){when{adding||editor!=null->{adding=false;editor=null};purchaseAsset!=null->purchaseAsset=null;else->selectedKind=null}}
 if(purchaseAsset!=null){GoldPurchaseEntry(vm,purchaseAsset!!,{purchaseAsset=null},{purchaseAsset=null});return}
 if(adding||editor!=null){AssetEditor(vm,editor,selectedKind?:AssetKind.GOLD,{adding=false;editor=null},{adding=false;editor=null});return}
 val allActive=assets.filter{it.active}
 if(selectedKind==null){AssetCategoryLanding(allActive,settings.moneyUnit,onSelect={selectedKind=it},onAdd={adding=true});return}
 val active=allActive.filter{it.kind==selectedKind};val totalBuy=active.sumOf{it.purchasePriceRial};val totalNow=active.sumOf{it.currentValueRial};val profit=totalNow-totalBuy
 Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton({selectedKind=null}){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به انواع دارایی")};Text(assetKindTitle(selectedKind!!),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  if(selectedKind==AssetKind.STOCK&&stockDrafts.isNotEmpty()){Text("معاملات پیامکی منتظر تأیید",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);stockDrafts.forEach{draft->StockDraftCard(draft,settings.moneyUnit,onConfirm={confirmStockDraft(draft)},onIgnore={scope.launch{vm.repo.db.stockDao().updateDraft(draft.copy(status=StockDraftStatus.IGNORED))}})}}
  Card(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=.88f),MaterialTheme.colorScheme.tertiary.copy(alpha=.72f)))).padding(20.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text("ارزش روز کل دارایی‌ها",color=Color.White.copy(.85f));Text(Money.format(totalNow,settings.moneyUnit),color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);HorizontalDivider(color=Color.White.copy(.25f));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("سرمایه اولیه",color=Color.White.copy(.8f));Text(Money.format(totalBuy,settings.moneyUnit),color=Color.White)};Column(horizontalAlignment=Alignment.End){Text(if(profit>=0)"سود کل" else "زیان کل",color=Color.White.copy(.8f));Text(Money.format(kotlin.math.abs(profit),settings.moneyUnit),color=Color.White,fontWeight=FontWeight.Bold)}}}}
  Text("موارد ثبت‌شده",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
  Text("${Digits.toPersian(active.size.toString())} دارایی فعال در این بخش",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Text("دارایی‌های من",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(active.isEmpty())Card(Modifier.fillMaxWidth()){Column(Modifier.padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("هنوز دارایی ثبت نشده است");Text("با دکمه + اولین دارایی را اضافه کنید",style=MaterialTheme.typography.bodySmall)}}
  active.forEach{asset->SwipeActionRow(onDelete={pendingDelete=asset},onEdit={editor=asset}){AssetGraphicCard(asset,trades.filter{it.assetId==asset.id},settings.moneyUnit,active.sumOf{it.currentValueRial},onAddPurchase={purchaseAsset=asset},onUpdate={value->scope.launch{vm.repo.db.assetDao().update(asset.copy(currentValueRial=value))}},onSale={scope.launch{vm.repo.db.assetDao().insertTrade(AssetTradeEntity(assetId=asset.id,isSale=true,quantity=asset.quantity,amountRial=asset.currentValueRial,tradedAt=System.currentTimeMillis()));vm.repo.db.assetDao().update(asset.copy(active=false))}})}}
 }
 FloatingActionButton({adding=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن دارایی")}}
 pendingDelete?.let{asset->AlertDialog(onDismissRequest={pendingDelete=null},title={Text("حذف دارایی")},text={Text("آیا «${asset.title}» و سوابق خرید و فروش آن حذف شود؟")},confirmButton={TextButton({pendingDelete=null;scope.launch{vm.repo.db.assetDao().deleteTrades(asset.id);vm.repo.db.assetDao().delete(asset.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton({pendingDelete=null}){Text("انصراف")}})}
}

private fun assetKindTitle(kind:Int)=when(kind){AssetKind.GOLD->"دارایی‌های فلزی (طلا، نقره و ...)";AssetKind.VEHICLE->"خودروها";AssetKind.PROPERTY->"املاک، خانه و زمین";AssetKind.CURRENCY->"دارایی‌های ارزی";AssetKind.STOCK->"بورس و سهام ایران";else->"سایر دارایی‌ها"}

@Composable private fun AssetCategoryLanding(assets:List<AssetEntity>,unit:ir.kharjyar.app.core.money.MoneyUnit,onSelect:(Int)->Unit,onAdd:()->Unit){
 val kinds=listOf(AssetKind.GOLD,AssetKind.CURRENCY,AssetKind.STOCK,AssetKind.VEHICLE,AssetKind.PROPERTY,AssetKind.OTHER)
 val colors=mapOf(AssetKind.GOLD to Color(0xFFFFB300),AssetKind.VEHICLE to Color(0xFF3185D8),AssetKind.PROPERTY to Color(0xFF8C5BC2),AssetKind.CURRENCY to Color(0xFF00A884),AssetKind.STOCK to Color(0xFF536DFE),AssetKind.OTHER to Color(0xFF26A69A))
 val total=assets.sumOf{it.currentValueRial}
 Box(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Text("دارایی‌ها",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
   Card(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(.88f),MaterialTheme.colorScheme.tertiary.copy(.72f)))).padding(20.dp)){Text("ارزش روز کل دارایی‌ها",color=Color.White.copy(.85f));Text(Money.format(total,unit),color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)}}
   Text("انواع دارایی",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
   kinds.forEach{kind->val list=assets.filter{it.kind==kind};val color=colors.getValue(kind);Card(Modifier.fillMaxWidth().clickable{onSelect(kind)},shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){AssetKindGraphic(kind,color);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(assetKindTitle(kind),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("${Digits.toPersian(list.size.toString())} مورد • ${Money.format(list.sumOf{it.currentValueRial},unit)}",style=MaterialTheme.typography.bodySmall)};Text("←",color=color,style=MaterialTheme.typography.titleLarge)}}}
  }
  FloatingActionButton(onClick=onAdd,modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن دارایی")}
 }
}

@Composable private fun AssetKindGraphic(kind:Int,color:Color,modifier:Modifier=Modifier){
 val icon=when(kind){AssetKind.GOLD->Icons.Filled.ViewInAr;AssetKind.VEHICLE->Icons.Filled.DirectionsCar;AssetKind.PROPERTY->Icons.Filled.HomeWork;AssetKind.CURRENCY->Icons.Filled.CurrencyExchange;AssetKind.STOCK->Icons.Filled.ShowChart;else->Icons.Filled.AutoAwesomeMosaic}
 val description=when(kind){AssetKind.GOLD->"شمش فلز گران‌بها";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"خانه و ملک";AssetKind.CURRENCY->"ارز خارجی";AssetKind.STOCK->"سبد سهام بورس ایران";else->"دارایی"}
 Box(modifier.size(54.dp).background(Brush.linearGradient(listOf(color,color.copy(alpha=.62f))),RoundedCornerShape(17.dp)),contentAlignment=Alignment.Center){Box(Modifier.size(40.dp).background(Color.White.copy(alpha=.14f),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Icon(icon,description,tint=Color.White,modifier=Modifier.size(30.dp))}}
}

@Composable private fun StockDraftCard(draft:StockSmsDraftEntity,unit:ir.kharjyar.app.core.money.MoneyUnit,onConfirm:()->Unit,onIgnore:()->Unit){val buy=draft.side==StockSide.BUY;Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=(if(buy)Color(0xFF1FA66A)else Color(0xFFE05260)).copy(.10f))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("${if(buy)"خرید" else "فروش"} ${draft.symbol}",fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleMedium);Text(draft.broker,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(PersianDate.fromMillis(draft.occurredAt).format(),style=MaterialTheme.typography.labelSmall)};Text("${Digits.toPersian(draft.quantity.toString())} سهم × ${Money.format(draft.unitPriceRial,unit)}");Text("ارزش معامله: ${Money.format(draft.totalRial,unit)}",fontWeight=FontWeight.Bold);Text("این پیامک هنوز روی سبد اثر نگذاشته است. اطلاعات را بررسی و تأیید کنید.",style=MaterialTheme.typography.bodySmall);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onConfirm,Modifier.weight(1f)){Text("تأیید و اعمال")};OutlinedButton(onIgnore,Modifier.weight(1f)){Text("نادیده گرفتن")}}}}}

@Composable private fun MiniAssetStat(label:String,value:String,color:Color){Card(shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(horizontal=14.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(value),color=color,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.labelSmall)}}}

@Composable private fun AssetGraphicCard(asset:AssetEntity,trades:List<AssetTradeEntity>,unit:ir.kharjyar.app.core.money.MoneyUnit,portfolioTotal:Long,onAddPurchase:()->Unit,onUpdate:(Long)->Unit,onSale:()->Unit){val accent=when(asset.kind){AssetKind.GOLD->Color(0xFFFFB300);AssetKind.VEHICLE->Color(0xFF3185D8);AssetKind.PROPERTY->Color(0xFF8C5BC2);AssetKind.CURRENCY->Color(0xFF00A884);AssetKind.STOCK->Color(0xFF536DFE);else->Color(0xFF26A69A)};val gain=asset.currentValueRial-asset.purchasePriceRial;var edited by remember(asset.id,asset.currentValueRial,unit){mutableStateOf((if(unit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(asset.currentValueRial)else asset.currentValueRial).toString())}
 Card(shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.72f))){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){AssetKindGraphic(asset.kind,accent);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(asset.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text((if(asset.kind==AssetKind.CURRENCY)"ارز خارجی • "+asset.note.removePrefix("CURRENCY:") else asset.note).ifBlank{when(asset.kind){AssetKind.GOLD->"طلا، سکه و فلزات گران‌بها";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"ملک";AssetKind.CURRENCY->"دارایی ارزی";else->"دارایی شخصی"}},color=MaterialTheme.colorScheme.onSurfaceVariant)};Column(horizontalAlignment=Alignment.End){Text("ارزش روز",style=MaterialTheme.typography.labelSmall);Text(Money.format(asset.currentValueRial,unit),fontWeight=FontWeight.Bold,color=accent)}};HorizontalDivider();Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("خرید ${PersianDate.fromMillis(asset.purchasedAt).format()}: ${Money.format(asset.purchasePriceRial,unit)}",style=MaterialTheme.typography.bodySmall);Text((if(gain>=0)"▲ " else "▼ ")+Money.format(kotlin.math.abs(gain),unit),color=if(gain>=0)Color(0xFF20A565) else Color(0xFFE14B55),fontWeight=FontWeight.Bold)};if(asset.kind==AssetKind.CURRENCY){Text("موجودی: ${Digits.toPersian(String.format(java.util.Locale.US,"%.2f",asset.quantity))} ${asset.note.removePrefix("CURRENCY:")} • نرخ ضمنی امروز: ${Money.format((asset.currentValueRial/(asset.quantity.takeIf{it>0}?:1.0)).toLong(),unit)}",style=MaterialTheme.typography.bodySmall)};if(asset.kind==AssetKind.STOCK){val p=asset.note.split('|');val avg=(asset.purchasePriceRial/(asset.quantity.takeIf{it>0}?:1.0)).toLong();val now=(asset.currentValueRial/(asset.quantity.takeIf{it>0}?:1.0)).toLong();val pct=if(asset.purchasePriceRial>0)gain*100.0/asset.purchasePriceRial else 0.0;val realized=trades.filter{it.isSale}.sumOf{it.amountRial-(it.quantity*avg).toLong()};Text("کارگزاری: ${p.getOrNull(1).orEmpty()} • موجودی: ${Digits.toPersian(asset.quantity.toLong().toString())} سهم");Text("وزن در سبد: ${Digits.toPersian(String.format(java.util.Locale.US,"%.1f",if(portfolioTotal>0)asset.currentValueRial*100.0/portfolioTotal else 0.0))}٪");Text("میانگین خرید: ${Money.format(avg,unit)} • قیمت فعلی: ${Money.format(now,unit)}",style=MaterialTheme.typography.bodySmall);Text("بازده تحقق‌نیافته: ${Digits.toPersian(String.format(java.util.Locale.US,"%.1f",pct))}٪ • سود/زیان تحقق‌یافته: ${Money.format(realized,unit)}",style=MaterialTheme.typography.bodySmall);if(trades.isNotEmpty()){Text("آخرین معاملات",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium);trades.sortedByDescending{it.tradedAt}.take(3).forEach{Text("${if(it.isSale)"فروش" else "خرید"} ${Digits.toPersian(it.quantity.toLong().toString())} سهم • ${Money.format(it.amountRial,unit)} • ${PersianDate.fromMillis(it.tradedAt).format()}",style=MaterialTheme.typography.labelSmall)}}};if(asset.kind==AssetKind.GOLD){val purchases=trades.filter{!it.isSale};val average=if(asset.quantity>0)asset.purchasePriceRial/asset.quantity else 0.0;Text("وزن کل: ${Digits.toPersian(String.format(java.util.Locale.US,"%.3f",asset.quantity))} گرم • ${Digits.toPersian((if(purchases.isEmpty())1 else purchases.size).toString())} مرحله خرید");Text("میانگین موزون خرید هر گرم: ${Money.format(average.toLong(),unit)}",style=MaterialTheme.typography.bodySmall);OutlinedButton(onAddPurchase,Modifier.fillMaxWidth()){Text("افزودن خرید جدید فلز")}};AmountTextField(edited,{edited=it},"بروزرسانی ارزش روز",unit=unit);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({onUpdate(Money.inputToRial(edited,unit)?:asset.currentValueRial)},Modifier.weight(1f)){Text("ذخیره ارزش")};OutlinedButton(onSale,Modifier.weight(1f)){Text("ثبت فروش")}};Text("برای ویرایش به راست و برای حذف به چپ بکشید",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
}

@Composable
private fun AssetEditor(vm:AppViewModel,existing:AssetEntity?,initialKind:Int,onDone:()->Unit,onCancel:()->Unit){

 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val settings by vm.settings.collectAsState()
 var kind by remember{mutableStateOf(existing?.kind?:initialKind)}
 var title by remember{mutableStateOf(existing?.title.orEmpty())}
 var quantity by remember{mutableStateOf(TextFieldValue(existing?.quantity?.toString() ?: "1"))}
 var quantityHadFocus by remember{mutableStateOf(false)}
 var purchaseDate by remember(existing?.id){mutableStateOf(existing?.let{PersianDate.fromMillis(it.purchasedAt)}?:PersianDate.today())}
 var purchase by remember(existing?.id,settings.moneyUnit){mutableStateOf(existing?.purchasePriceRial?.let{if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(it).toString()else it.toString()}.orEmpty())}
 var current by remember(existing?.id,settings.moneyUnit){mutableStateOf(existing?.currentValueRial?.let{if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(it).toString()else it.toString()}.orEmpty())}
 val kinds=listOf(AssetKind.GOLD,AssetKind.CURRENCY,AssetKind.STOCK,AssetKind.VEHICLE,AssetKind.PROPERTY,AssetKind.OTHER)
 val goldTypes=listOf("طلای آب‌شده","طلای شکسته/دست‌دوم","طلای نو","سکه امامی (طرح جدید)","سکه تمام بهار آزادی (طرح قدیم)","نیم سکه بهار آزادی","ربع سکه بهار آزادی","سکه گرمی بانک مرکزی","سکه پارسیان ۱۸ عیار (وزن آزاد)","نقره","پلاتین","سایر فلزات گران‌بها")
 var goldType by remember{mutableStateOf(existing?.note?.takeIf{it in goldTypes}?:goldTypes.first())}
 val currencies=listOf("USD" to "دلار آمریکا","EUR" to "یورو","GBP" to "پوند انگلیس","AED" to "درهم امارات","TRY" to "لیر ترکیه","CAD" to "دلار کانادا","AUD" to "دلار استرالیا","CHF" to "فرانک سوئیس","CNY" to "یوان چین")
 var currencyCode by remember{mutableStateOf(existing?.note?.removePrefix("CURRENCY:")?.takeIf{code->currencies.any{it.first==code}}?:"USD")}
 var unitRate by remember{mutableStateOf("")}
 val stockParts=existing?.note?.split('|').orEmpty()
 var stockBroker by remember{mutableStateOf(stockParts.getOrNull(1).orEmpty())}
 var stockSymbol by remember{mutableStateOf(stockParts.getOrNull(2).orEmpty())}
 var stockUnitPrice by remember{mutableStateOf("")}
 var stockCurrentPrice by remember{mutableStateOf(if(existing?.kind==AssetKind.STOCK&&existing.quantity>0)((if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole((existing.currentValueRial/existing.quantity).toLong())else(existing.currentValueRial/existing.quantity).toLong()).toString())else "")}
 var gramPrice by remember{mutableStateOf("")}
 var purchaseGramRate by remember{mutableStateOf("")}
 var lookup by remember{mutableStateOf<String?>(null)}

 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
  TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به سبد دارایی")}
  Text(if(existing==null)"افزودن دارایی" else "ویرایش دارایی",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  ComboBox("نوع دارایی",kinds,kind,{if(existing==null)kind=it},labelOf={when(it){AssetKind.GOLD->"فلزات گران‌بها";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"ملک";AssetKind.CURRENCY->"دارایی ارزی";else->"دارایی شخصی"}})
  OutlinedTextField(title,{title=it},label={Text("عنوان دارایی")},modifier=Modifier.fillMaxWidth())
  if(kind==AssetKind.GOLD){
   ComboBox("نوع طلا یا سکه",goldTypes,goldType,{goldType=it},labelOf={it})
   val coin=goldType.contains("سکه")
   OutlinedTextField(quantity,{quantity=it.copy(text=Digits.normalize(it.text))},label={Text(if(coin)"تعداد سکه" else "وزن (گرم)")},modifier=Modifier.fillMaxWidth().onFocusChanged{state->if(state.isFocused&&!quantityHadFocus){quantityHadFocus=true;quantity=quantity.copy(selection=TextRange(0,quantity.text.length))}else if(!state.isFocused)quantityHadFocus=false})
  }else if(kind==AssetKind.CURRENCY){
   ComboBox("نوع ارز",currencies.map{it.first},currencyCode,{currencyCode=it},labelOf={code->currencies.first{it.first==code}.second+" ($code)"})
   OutlinedTextField(quantity,{quantity=it.copy(text=Digits.normalize(it.text))},label={Text("تعداد واحد ارز")},modifier=Modifier.fillMaxWidth())
  }else if(kind==AssetKind.STOCK){
   OutlinedTextField(stockBroker,{stockBroker=it},label={Text("نام کارگزاری")},modifier=Modifier.fillMaxWidth())
   OutlinedTextField(stockSymbol,{stockSymbol=it.trim()},label={Text("نام یا نماد سهام")},modifier=Modifier.fillMaxWidth())
   OutlinedTextField(quantity,{quantity=it.copy(text=Digits.normalize(it.text))},label={Text("تعداد سهم")},modifier=Modifier.fillMaxWidth())
  }else OutlinedTextField(quantity,{quantity=it.copy(text=Digits.normalize(it.text))},label={Text("تعداد")},modifier=Modifier.fillMaxWidth().onFocusChanged{state->if(state.isFocused&&!quantityHadFocus){quantityHadFocus=true;quantity=quantity.copy(selection=TextRange(0,quantity.text.length))}else if(!state.isFocused)quantityHadFocus=false})

  HorizontalDivider()
  Text("اطلاعات خرید",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
  Text("تاریخ و مبلغ واقعی خرید را مطابق همان زمان، به‌صورت دستی وارد کنید. این مبلغ با استعلام روز تغییر نمی‌کند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  PersianDateField(purchaseDate,{purchaseDate=it})
  if(kind==AssetKind.GOLD&&!goldType.contains("سکه")){
   AmountTextField(purchaseGramRate,{purchaseGramRate=it},"نرخ خرید هر گرم",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   val historicalRateInput=Digits.parseAmount(purchaseGramRate)?:0L
   val purchaseWeight=quantity.text.toDoubleOrNull()?:0.0
   Button({purchase=(purchaseWeight*historicalRateInput).toLong().toString()},enabled=purchaseWeight>0&&historicalRateInput>0,modifier=Modifier.fillMaxWidth()){Text("محاسبه قیمت کل خرید")}
  }else if(kind==AssetKind.CURRENCY){
   AmountTextField(unitRate,{unitRate=it},"نرخ خرید هر واحد ارز",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   Button({val rate=Money.inputToRial(unitRate,settings.moneyUnit)?:0;val total=((quantity.text.toDoubleOrNull()?:0.0)*rate).toLong();purchase=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(total)else total).toString()},enabled=(quantity.text.toDoubleOrNull()?:0.0)>0&&(Money.inputToRial(unitRate,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("محاسبه مبلغ کل خرید ارز")}
  }else if(kind==AssetKind.STOCK){
   AmountTextField(stockUnitPrice,{stockUnitPrice=it},"میانگین قیمت خرید هر سهم",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   Button({val rate=Money.inputToRial(stockUnitPrice,settings.moneyUnit)?:0;val total=((quantity.text.toDoubleOrNull()?:0.0)*rate).toLong();purchase=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(total)else total).toString()},enabled=(quantity.text.toDoubleOrNull()?:0.0)>0&&(Money.inputToRial(stockUnitPrice,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("محاسبه ارزش خرید سهام")}
  }
  AmountTextField(purchase,{purchase=it},"قیمت خرید کل در تاریخ خرید",Modifier.fillMaxWidth(),unit=settings.moneyUnit)

  HorizontalDivider()
  Text("ارزش امروز",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
  if(kind==AssetKind.GOLD&&!goldType.contains("سکه")){
   if(goldType.startsWith("طلا"))OutlinedButton({scope.launch{
    val value=GoldPriceService.gram18Rial()
    if(value!=null){gramPrice=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(value)else value).toString();lookup="نرخ روز طلای ۱۸ عیار ایران دریافت شد"}
    else lookup="اتصال برقرار نشد؛ نرخ روز را دستی وارد کنید"
   }},Modifier.fillMaxWidth()){Text("استعلام نرخ روز طلای ۱۸ عیار ایران")}
   AmountTextField(gramPrice,{gramPrice=it},"نرخ روز هر گرم فلز",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   lookup?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
   Button({
    val weight=quantity.text.toDoubleOrNull()?:0.0
    val rate=Money.inputToRial(gramPrice,settings.moneyUnit)?:0
    val calculated=IranianGoldCalculator.rawOrUsed(weight,rate)
    current=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(calculated)else calculated).toString()
   },Modifier.fillMaxWidth(),enabled=(quantity.text.toDoubleOrNull()?:0.0)>0&&(Money.inputToRial(gramPrice,settings.moneyUnit)?:0)>0){Text("محاسبه ارزش روز فلز")}
   Text("ارزش روز بر اساس وزن و نرخ روز محاسبه می‌شود؛ قیمت تاریخی خرید بدون تغییر باقی می‌ماند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }else if(kind==AssetKind.GOLD){
   Text("قیمت روز کل این نوع سکه را از بازار وارد کنید؛ قیمت خرید تاریخی و تاریخ آن بدون تغییر حفظ می‌شود.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }else if(kind==AssetKind.CURRENCY){
   OutlinedButton({scope.launch{val value=GoldPriceService.currencyRial(currencyCode);if(value!=null){unitRate=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(value)else value).toString();lookup="نرخ آزاد ${currencies.first{it.first==currencyCode}.second} دریافت شد"}else lookup="استعلام در دسترس نبود؛ نرخ را دستی وارد کنید"}},Modifier.fillMaxWidth()){Text("استعلام آنلاین نرخ روز ارز")}
   AmountTextField(unitRate,{unitRate=it},"نرخ امروز هر واحد ارز",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   Button({val rate=Money.inputToRial(unitRate,settings.moneyUnit)?:0;val total=((quantity.text.toDoubleOrNull()?:0.0)*rate).toLong();current=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(total)else total).toString()},enabled=(quantity.text.toDoubleOrNull()?:0.0)>0&&(Money.inputToRial(unitRate,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("محاسبه ارزش امروز ارز")}
   lookup?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
   Text("نرخ آنلاین قابل ویرایش است و در صورت قطع اینترنت می‌توانید نرخ بازار را دستی وارد کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }else if(kind==AssetKind.STOCK){
   AmountTextField(stockCurrentPrice,{stockCurrentPrice=it},"قیمت فعلی هر سهم",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
   Button({val rate=Money.inputToRial(stockCurrentPrice,settings.moneyUnit)?:0;val total=((quantity.text.toDoubleOrNull()?:0.0)*rate).toLong();current=(if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN)Money.rialToTomanWhole(total)else total).toString()},enabled=(quantity.text.toDoubleOrNull()?:0.0)>0&&(Money.inputToRial(stockCurrentPrice,settings.moneyUnit)?:0)>0,modifier=Modifier.fillMaxWidth()){Text("محاسبه ارزش فعلی سهام")}
  }else{
   Text(if(kind==AssetKind.VEHICLE)"قیمت روز خودرو را کاربر وارد می‌کند." else if(kind==AssetKind.PROPERTY)"قیمت روز ملک را کاربر وارد می‌کند." else "ارزش روز دارایی را وارد کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  AmountTextField(current,{current=it},"ارزش روز کل",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
  val purchaseValue=Money.inputToRial(purchase,settings.moneyUnit)?:0
  val currentValue=Money.inputToRial(current,settings.moneyUnit)?:0
  if(purchaseValue>0&&currentValue>0){
   val difference=currentValue-purchaseValue
   Card(colors=CardDefaults.cardColors(containerColor=(if(difference>=0)Color(0xFF20A565) else Color(0xFFE14B55)).copy(alpha=.10f)),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(12.dp)){Text(if(difference>=0)"سود فعلی" else "زیان فعلی",fontWeight=FontWeight.Bold,color=if(difference>=0)Color(0xFF20A565) else Color(0xFFE14B55));Text(Money.format(kotlin.math.abs(difference),settings.moneyUnit));Text("نسبت تغییر: ${String.format(java.util.Locale.US,"%.1f",difference.toDouble()/purchaseValue*100)}٪")}
   }
  }
  Button({scope.launch{
   val value=AssetEntity(id=existing?.id?:0,kind=kind,title=if(kind==AssetKind.STOCK)stockSymbol else title.trim(),quantity=quantity.text.toDoubleOrNull()?:1.0,purchasePriceRial=purchaseValue,currentValueRial=currentValue,purchasedAt=purchaseDate.startOfDayMillis(),note=when(kind){AssetKind.GOLD->goldType;AssetKind.CURRENCY->"CURRENCY:$currencyCode";AssetKind.STOCK->"STOCK|${stockBroker.trim()}|${stockSymbol.trim()}|${if((quantity.text.toDoubleOrNull()?:0.0)>0)purchaseValue/(quantity.text.toDoubleOrNull()?:1.0) else 0}";else->existing?.note.orEmpty()},active=existing?.active?:true)
   if(existing==null){val assetId=vm.repo.db.assetDao().insert(value);if(kind==AssetKind.GOLD)vm.repo.db.assetDao().insertTrade(AssetTradeEntity(assetId=assetId,isSale=false,quantity=value.quantity,amountRial=value.purchasePriceRial,tradedAt=value.purchasedAt))}else vm.repo.db.assetDao().update(value)
   showSavedMessage(context,"دارایی")
   onDone()
  }},enabled=(if(kind==AssetKind.STOCK)stockSymbol.isNotBlank()&&stockBroker.isNotBlank() else title.isNotBlank())&&purchaseValue>0&&currentValue>0,modifier=Modifier.fillMaxWidth()){Text(if(existing==null)"ثبت دارایی" else "ذخیره تغییرات")}
 }
}

@Composable
private fun GoldPurchaseEntry(vm:AppViewModel,asset:AssetEntity,onDone:()->Unit,onCancel:()->Unit){
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val settings by vm.settings.collectAsState()
 var gramsText by remember{mutableStateOf("")}
 var rateText by remember{mutableStateOf("")}
 var date by remember{mutableStateOf(PersianDate.today())}
 val grams=Digits.normalize(gramsText).replace('،','.').toDoubleOrNull()?:0.0
 val rateRial=Money.inputToRial(rateText,settings.moneyUnit)?:0L
 val paid=(grams*rateRial).toLong()
 val newWeight=asset.quantity+grams
 val newCost=asset.purchasePriceRial+paid
 val weightedAverage=if(newWeight>0)newCost/newWeight else 0.0
 val currentRate=if(asset.quantity>0)asset.currentValueRial/asset.quantity else 0.0
 val projectedCurrent=(currentRate*newWeight).toLong()
 val projectedGain=projectedCurrent-newCost
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به دارایی‌ها")}
  Text("افزودن مرحله خرید فلز",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text(asset.title,style=MaterialTheme.typography.titleMedium,color=Color(0xFFFFA000))
  Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("وضعیت فعلی");Text("وزن: ${Digits.toPersian(String.format(java.util.Locale.US,"%.3f",asset.quantity))} گرم");Text("جمع هزینه خرید: ${Money.format(asset.purchasePriceRial,settings.moneyUnit)}");if(asset.quantity>0)Text("میانگین خرید هر گرم: ${Money.format((asset.purchasePriceRial/asset.quantity).toLong(),settings.moneyUnit)}")}}
  Text("تاریخ این خرید")
  PersianDateField(date,{date=it})
  OutlinedTextField(gramsText,{gramsText=Digits.normalize(it)},label={Text("وزن خرید جدید (گرم)")},modifier=Modifier.fillMaxWidth())
  AmountTextField(rateText,{rateText=it},"نرخ خرید هر گرم",Modifier.fillMaxWidth(),unit=settings.moneyUnit)
  if(paid>0){Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFB300).copy(alpha=.10f)),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("مبلغ این خرید: ${Money.format(paid,settings.moneyUnit)}",fontWeight=FontWeight.Bold);Text("وزن کل پس از خرید: ${Digits.toPersian(String.format(java.util.Locale.US,"%.3f",newWeight))} گرم");Text("میانگین موزون جدید هر گرم: ${Money.format(weightedAverage.toLong(),settings.moneyUnit)}");if(currentRate>0){Text("ارزش کل با نرخ روز فعلی: ${Money.format(projectedCurrent,settings.moneyUnit)}");Text(if(projectedGain>=0)"سود کل: ${Money.format(projectedGain,settings.moneyUnit)}" else "زیان کل: ${Money.format(kotlin.math.abs(projectedGain),settings.moneyUnit)}",color=if(projectedGain>=0)Color(0xFF20A565) else Color(0xFFE14B55))}}}}
  Button({scope.launch{vm.repo.db.assetDao().insertTrade(AssetTradeEntity(assetId=asset.id,isSale=false,quantity=grams,amountRial=paid,tradedAt=date.startOfDayMillis()));vm.repo.db.assetDao().update(asset.copy(quantity=newWeight,purchasePriceRial=newCost,currentValueRial=projectedCurrent));showSavedMessage(context,"مرحله جدید خرید فلز");onDone()}},enabled=grams>0&&rateRial>0,modifier=Modifier.fillMaxWidth()){Text("ثبت این مرحله خرید")}
 }
}

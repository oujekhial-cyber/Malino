package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.assets.GoldPriceService
import ir.kharjyar.app.assets.IranianGoldCalculator
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.ComboBox
import kotlinx.coroutines.launch

@Composable fun AssetsScreen(vm:AppViewModel) {
 val assets by vm.assets.collectAsState();val settings by vm.settings.collectAsState();val scope=rememberCoroutineScope()
 var adding by remember{mutableStateOf(false)};var kind by remember{mutableStateOf(AssetKind.GOLD)};var title by remember{mutableStateOf("")};var quantity by remember{mutableStateOf("1")};var purchase by remember{mutableStateOf("")};var current by remember{mutableStateOf("")}
 val kinds=listOf(AssetKind.GOLD,AssetKind.VEHICLE,AssetKind.PROPERTY,AssetKind.OTHER);val goldTypes=listOf("طلای آب‌شده","طلای شکسته/دست‌دوم","طلای نو");var goldType by remember{mutableStateOf(goldTypes.first())};var gramPrice by remember{mutableStateOf("")};var wage by remember{mutableStateOf("10")};var profit by remember{mutableStateOf("7")};var vat by remember{mutableStateOf("10")};var lookupMessage by remember{mutableStateOf<String?>(null)}
 Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Text(if(adding) "افزودن دارایی" else "دارایی‌ها",style=MaterialTheme.typography.headlineSmall)
  if(!adding) { val active=assets.filter{it.active};val buy=active.sumOf{it.purchasePriceRial};val now=active.sumOf{it.currentValueRial};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("ارزش کل: ${Money.format(now,settings.moneyUnit)}");Text("سود/زیان: ${Money.format(now-buy,settings.moneyUnit)}",color=if(now>=buy)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}};active.forEach{a->var edited by remember(a.id){mutableStateOf(a.currentValueRial.toString())};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(a.title,style=MaterialTheme.typography.titleMedium);Text("خرید: ${Money.format(a.purchasePriceRial,settings.moneyUnit)}");Text("سود/زیان: ${Money.format(a.currentValueRial-a.purchasePriceRial,settings.moneyUnit)}");AmountTextField(edited,{edited=it},"ارزش روز");Row{Button({scope.launch{vm.repo.db.assetDao().update(a.copy(currentValueRial=Digits.parseAmount(edited)?:a.currentValueRial))}}){Text("بروزرسانی")};TextButton({scope.launch{vm.repo.db.assetDao().insertTrade(AssetTradeEntity(assetId=a.id,isSale=true,quantity=a.quantity,amountRial=a.currentValueRial,tradedAt=System.currentTimeMillis()));vm.repo.db.assetDao().update(a.copy(active=false))}}){Text("ثبت فروش")}}}}} }
  else { ComboBox("نوع دارایی",kinds,kind,{kind=it},labelOf={when(it){AssetKind.GOLD->"طلا";AssetKind.VEHICLE->"خودرو";AssetKind.PROPERTY->"ملک";else->"دارایی دستی"}})
   if(kind==AssetKind.GOLD){ComboBox("نوع طلا",goldTypes,goldType,{goldType=it},labelOf={it});OutlinedButton({scope.launch{val p=GoldPriceService.gram18Rial();if(p!=null){gramPrice=p.toString();lookupMessage="نرخ طلای ۱۸ عیار ایران دریافت شد"}else lookupMessage="اتصال برقرار نشد؛ نرخ را دستی وارد کنید"}},Modifier.fillMaxWidth()){Text("استعلام قیمت طلای ۱۸ عیار ایران")};AmountTextField(gramPrice,{gramPrice=it},"قیمت هر گرم طلای ۱۸ عیار (ریال)",Modifier.fillMaxWidth());lookupMessage?.let{Text(it,color=if(gramPrice.isNotBlank())MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)};OutlinedTextField(quantity,{quantity=Digits.normalize(it)},label={Text("وزن (گرم)")},modifier=Modifier.fillMaxWidth());if(goldType=="طلای نو"){OutlinedTextField(wage,{wage=Digits.normalize(it)},label={Text("اجرت ساخت (درصد)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(profit,{profit=Digits.normalize(it)},label={Text("سود فروشنده (درصد)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(vat,{vat=Digits.normalize(it)},label={Text("مالیات اجرت و سود (درصد)")},modifier=Modifier.fillMaxWidth())};Button({val w=quantity.toDoubleOrNull()?:0.0;val p=Digits.parseAmount(gramPrice)?:0;purchase=(if(goldType=="طلای نو")IranianGoldCalculator.newGold(w,p,wage.toDoubleOrNull()?:0.0,profit.toDoubleOrNull()?:0.0,vat.toDoubleOrNull()?:0.0) else IranianGoldCalculator.rawOrUsed(w,p)).toString();current=IranianGoldCalculator.rawOrUsed(w,p).toString()},modifier=Modifier.fillMaxWidth()){Text("محاسبه قیمت")}}
   else OutlinedTextField(quantity,{quantity=Digits.normalize(it)},label={Text("تعداد")},modifier=Modifier.fillMaxWidth())
   OutlinedTextField(title,{title=it},label={Text("عنوان دارایی")},modifier=Modifier.fillMaxWidth());AmountTextField(purchase,{purchase=it},"قیمت خرید کل",Modifier.fillMaxWidth());AmountTextField(current,{current=it},"ارزش روز",Modifier.fillMaxWidth());Button({scope.launch{vm.repo.db.assetDao().insert(AssetEntity(kind=kind,title=title,quantity=quantity.toDoubleOrNull()?:1.0,purchasePriceRial=Digits.parseAmount(purchase)?:0,currentValueRial=Digits.parseAmount(current)?:0,purchasedAt=System.currentTimeMillis(),note=if(kind==AssetKind.GOLD)goldType else ""));adding=false;title="";purchase="";current=""}},enabled=title.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("ثبت دارایی")};TextButton({adding=false},Modifier.fillMaxWidth()){Text("انصراف")}
  }
 }
 if(!adding) FloatingActionButton({adding=true},Modifier.align(Alignment.BottomStart).padding(20.dp)){Text("+")}
 }
}

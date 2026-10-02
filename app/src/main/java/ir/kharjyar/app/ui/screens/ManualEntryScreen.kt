package ir.kharjyar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.*
import ir.kharjyar.app.ui.theme.LocalAppSkin
import kotlinx.coroutines.launch

/** فرم ساده ثبت تراکنش؛ موارد روزمره در نمای اصلی و گزینه‌های کم‌کاربرد در «جزئیات بیشتر» هستند. */
@Composable
fun ManualEntryScreen(viewModel:AppViewModel,nav:NavHostController,presetDirection:Int?=null,presetTransfer:Boolean=false){
 val context=androidx.compose.ui.platform.LocalContext.current;val settings by viewModel.settings.collectAsState();val accounts by viewModel.accounts.collectAsState();val categories by viewModel.categories.collectAsState();val defaultAccount by viewModel.defaultAccount.collectAsState();val scope=rememberCoroutineScope();val skin=LocalAppSkin.current
 val active=accounts.filter{!it.archived};var accountId by remember{mutableStateOf<Long?>(null)};var amountText by remember{mutableStateOf("")};var direction by remember{mutableStateOf(presetDirection?:TxDirection.WITHDRAW)};var categoryId by remember{mutableStateOf<Long?>(null)};var description by remember{mutableStateOf("")};var transferToOwn by remember{mutableStateOf(true)};var targetAccountId by remember{mutableStateOf<Long?>(null)};var detailsOpen by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};var receiptPath by remember{mutableStateOf("")}
 val now=remember{PersianDate.nowHourMinute()};var date by remember{mutableStateOf(PersianDate.today())};var hour by remember{mutableStateOf(now.first)};var minute by remember{mutableStateOf(now.second)}
 LaunchedEffect(active,defaultAccount){if(accountId==null)accountId=defaultAccount?.id?.takeIf{id->active.any{it.id==id}}?:active.firstOrNull()?.id}
 val tint=when{presetTransfer->skin.accent;direction==TxDirection.DEPOSIT->skin.incomeColor;else->skin.expenseColor}
 Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal=16.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Column(verticalArrangement=Arrangement.spacedBy(3.dp)){Text(if(presetTransfer)"انتقال وجه" else "ثبت تراکنش",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("فقط مبلغ و حساب را وارد کنید؛ بقیه موارد اختیاری‌اند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(!presetTransfer&&presetDirection==null) SimpleDirectionSelector(direction){direction=it}
  else SimpleTypeBanner(direction,presetTransfer,tint)
  Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.94f),border=androidx.compose.foundation.BorderStroke(1.dp,tint.copy(.22f)),tonalElevation=2.dp){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
   if(active.isEmpty()){Text("برای ثبت تراکنش ابتدا یک حساب اضافه کنید.",color=MaterialTheme.colorScheme.error);Button({nav.navigate("accountEdit/0")}){Text("افزودن حساب")}}
   else{
    Text("اطلاعات اصلی",fontWeight=FontWeight.Bold,color=tint)
    AmountTextField(value=amountText,onValueChange={amountText=it;error=null},label="مبلغ",unit=settings.moneyUnit,supportingText=Money.inputToRial(amountText,settings.moneyUnit)?.let{Money.format(it,settings.moneyUnit)},modifier=Modifier.fillMaxWidth())
    AccountPicker(active,accountId){accountId=it;error=null}
    if(presetTransfer){
     ComboBox(label="انتقال به",options=listOf(true,false),selected=transferToOwn,labelOf={if(it)"یکی از حساب‌های خودم" else "حساب شخص دیگر"},onSelect={transferToOwn=it;targetAccountId=null})
     if(transferToOwn){val targets=active.filter{it.id!=accountId};ComboBox(label="حساب مقصد",options=targets.map{it.id},selected=targetAccountId,labelOf={id->targets.firstOrNull{it.id==id}?.title?:"انتخاب حساب"},placeholder="انتخاب حساب مقصد",onSelect={targetAccountId=it})}
    }else CategoryPicker(categories,categoryId,if(direction==TxDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE,onCreate={name->scope.launch{categoryId=viewModel.repo.categoryDao.insert(CategoryEntity(name=name,colorArgb=0xFF6C8AE4))}}){categoryId=it}
    OutlinedTextField(value=description,onValueChange={description=it},label={Text(if(direction==TxDirection.DEPOSIT)"بابت چه بود؟ (اختیاری)" else "برای چه بود؟ (اختیاری)")},singleLine=true,modifier=Modifier.fillMaxWidth().keepAboveKeyboard())
   }
  }}
  if(active.isNotEmpty()){
   Surface(Modifier.fillMaxWidth().clickable{detailsOpen=!detailsOpen},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(.55f)){Row(Modifier.padding(horizontal=13.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.Tune,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(19.dp));Spacer(Modifier.width(8.dp));Text("جزئیات بیشتر",Modifier.weight(1f),fontWeight=FontWeight.Medium);Icon(if(detailsOpen)Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,null)}}
   AnimatedVisibility(detailsOpen){Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface.copy(.85f),border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("تاریخ و ساعت",style=MaterialTheme.typography.labelLarge);DatePickerRow(date,hour,minute,{date=it},{h,m->hour=h;minute=m});ReceiptImagePicker(receiptPath){receiptPath=it}}}}
   error?.let{Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
   Button(onClick={
    val acc=accountId;val amount=Money.inputToRial(amountText,settings.moneyUnit)
    when{acc==null->error="حساب را انتخاب کنید";amount==null||amount<=0->error="مبلغ معتبر وارد کنید";presetTransfer&&transferToOwn&&targetAccountId==null->error="حساب مقصد را انتخاب کنید";presetTransfer&&transferToOwn&&targetAccountId==acc->error="حساب مبدأ و مقصد نمی‌تواند یکی باشد";else->{error=null;scope.launch{val at=PersianDate.toMillis(date,hour,minute);val savedId=if(presetTransfer&&transferToOwn)viewModel.repo.addInternalTransfer(acc,requireNotNull(targetAccountId),amount,description.trim(),at).first else viewModel.repo.addManualTransaction(accountId=acc,amountRial=amount,direction=if(presetTransfer)TxDirection.WITHDRAW else direction,nature=if(presetTransfer)TxNature.TRANSFER else if(direction==TxDirection.DEPOSIT)TxNature.INCOME else TxNature.EXPENSE,categoryId=if(presetTransfer)null else categoryId,description=description.trim(),occurredAt=at,counterparty=if(presetTransfer)"حساب شخص دیگر" else "");if(receiptPath.isNotBlank())viewModel.repo.db.transactionAttachmentDao().insert(TransactionAttachmentEntity(transactionId=savedId,imagePath=receiptPath,createdAt=System.currentTimeMillis()));ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context);showSavedMessage(context,"تراکنش");nav.popBackStack()}}
    }
   },modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=tint)){Icon(Icons.Filled.Check,null);Spacer(Modifier.width(7.dp));Text(if(presetTransfer)"ثبت انتقال" else "ثبت تراکنش",fontWeight=FontWeight.Bold)}
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){TextButton({nav.navigate("quickAdd")}){Icon(Icons.Filled.Mic,null,Modifier.size(17.dp));Spacer(Modifier.width(5.dp));Text("ثبت سریع")};TextButton({nav.navigate("smsPaste")}){Icon(Icons.Filled.Sms,null,Modifier.size(17.dp));Spacer(Modifier.width(5.dp));Text("از پیامک")}}
  }
 }
}

@Composable private fun SimpleDirectionSelector(selected:Int,onSelect:(Int)->Unit){val skin=LocalAppSkin.current;Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(.55f),RoundedCornerShape(18.dp)).padding(5.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){DirectionChoice("برداشت","پول از حساب کم شد",Icons.Filled.ArrowUpward,skin.expenseColor,selected==TxDirection.WITHDRAW,Modifier.weight(1f)){onSelect(TxDirection.WITHDRAW)};DirectionChoice("واریز","پول به حساب آمد",Icons.Filled.ArrowDownward,skin.incomeColor,selected==TxDirection.DEPOSIT,Modifier.weight(1f)){onSelect(TxDirection.DEPOSIT)}}}
@Composable private fun DirectionChoice(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,tint:Color,selected:Boolean,modifier:Modifier,onClick:()->Unit){Row(modifier.clip(RoundedCornerShape(14.dp)).background(if(selected)tint.copy(.15f)else Color.Transparent).border(if(selected)1.dp else 0.dp,tint.copy(.45f),RoundedCornerShape(14.dp)).clickable(onClick=onClick).padding(horizontal=9.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(31.dp).background(tint.copy(.15f),CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=tint,modifier=Modifier.size(17.dp))};Spacer(Modifier.width(7.dp));Column{Text(title,fontWeight=FontWeight.Bold);Text(subtitle,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)}}}
@Composable private fun SimpleTypeBanner(direction:Int,transfer:Boolean,tint:Color){Row(Modifier.fillMaxWidth().background(tint.copy(.11f),RoundedCornerShape(16.dp)).border(1.dp,tint.copy(.32f),RoundedCornerShape(16.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(transfer)Icons.Filled.SwapHoriz else if(direction==TxDirection.DEPOSIT)Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,null,tint=tint);Spacer(Modifier.width(9.dp));Text(if(transfer)"جابه‌جایی بین حساب‌ها" else if(direction==TxDirection.DEPOSIT)"واریز به حساب" else "برداشت از حساب",fontWeight=FontWeight.Bold)}}

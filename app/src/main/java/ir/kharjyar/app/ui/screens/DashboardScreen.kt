package ir.kharjyar.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.balance.TxSummarizer
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.DirectionBadge
import ir.kharjyar.app.ui.components.EmptyState
import ir.kharjyar.app.ui.components.BankCard
import ir.kharjyar.app.ui.components.EnterCard
import ir.kharjyar.app.ui.components.ScreenEnterAnimation
import ir.kharjyar.app.ui.components.GlassSnackbarHost
import ir.kharjyar.app.ui.components.HeroCard
import androidx.compose.material3.SnackbarResult
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.SkinCard
import androidx.compose.material.icons.filled.SwapHoriz
import ir.kharjyar.app.ui.components.EmbossedText
import ir.kharjyar.app.ui.theme.LocalAppSkin

@Composable
fun LiveMarketGlass(unit:ir.kharjyar.app.core.money.MoneyUnit,onOpenMarket:()->Unit) {
    val scope=rememberCoroutineScope();val context=LocalContext.current;var expanded by remember{mutableStateOf(false)};var panelVisible by remember{mutableStateOf(false)};var refreshKey by remember{mutableStateOf(0)};var loading by remember{mutableStateOf(false)};var gold by remember{mutableStateOf<Long?>(null)};var dollar by remember{mutableStateOf<Long?>(null)};var previousGold by remember{mutableStateOf<Long?>(null)};var previousDollar by remember{mutableStateOf<Long?>(null)};var failed by remember{mutableStateOf(false)};var stale by remember{mutableStateOf(false)};var updatedAt by remember{mutableStateOf<Long?>(null)}
    val arrowRotation by animateFloatAsState(if(expanded)180f else 0f,animationSpec=tween(550),label="marketArrow")
    val darkMarket=MaterialTheme.colorScheme.background.luminance()<.5f
    val panelBase=if(darkMarket)Color(0xFF0B101B) else Color(0xFFF9FAFF)
    val muted=if(darkMarket)Color(0xFF9196A4) else Color(0xFF626978)
    // فاصله کافی از دکمه تا پنجره؛ خود پنجره دیگر تمام ارتفاع صفحه را نمی‌گیرد
    // تا سیستم‌عامل برای جا دادن آن، پنجره را به بالا و روی دکمه منتقل نکند.
    val marketBarGapPx=with(LocalDensity.current){10.dp.roundToPx()}
    fun closePanel(){scope.launch{panelVisible=false;kotlinx.coroutines.delay(420);expanded=false}}
    LaunchedEffect(expanded,refreshKey){if(!expanded)return@LaunchedEffect;panelVisible=true;loading=true;failed=false;val cachedGold=ir.kharjyar.app.assets.MarketPriceCache.read(context,"GOLD18");val cachedDollar=ir.kharjyar.app.assets.MarketPriceCache.read(context,"USD");if(gold==null)gold=cachedGold?.valueRial;if(dollar==null)dollar=cachedDollar?.valueRial;coroutineScope{val g=async{ir.kharjyar.app.assets.GoldPriceService.gram18Rial()};val d=async{ir.kharjyar.app.assets.GoldPriceService.dollarRial()};val newGold=g.await();val newDollar=d.await();val now=System.currentTimeMillis();if(gold!=null&&newGold!=null)previousGold=gold;if(dollar!=null&&newDollar!=null)previousDollar=dollar;if(newGold!=null){gold=newGold;ir.kharjyar.app.assets.MarketPriceCache.write(context,"GOLD18",newGold,now)};if(newDollar!=null){dollar=newDollar;ir.kharjyar.app.assets.MarketPriceCache.write(context,"USD",newDollar,now)};failed=newGold==null||newDollar==null;stale=failed;updatedAt=if(stale)listOfNotNull(if(newGold==null)cachedGold?.updatedAt else now,if(newDollar==null)cachedDollar?.updatedAt else now).minOrNull() else now};loading=false}
    Surface(modifier=Modifier.clickable{if(expanded)closePanel()else expanded=true},color=MaterialTheme.colorScheme.surface.copy(alpha=.78f),shape=RoundedCornerShape(50),border=BorderStroke(1.dp,MaterialTheme.colorScheme.primary.copy(alpha=.30f)),tonalElevation=5.dp,shadowElevation=5.dp){Row(Modifier.padding(horizontal=10.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Icon(Icons.Filled.ShowChart,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(16.dp));Text("نبض بازار",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium);Icon(Icons.Filled.ExpandMore,null,modifier=Modifier.size(16.dp).graphicsLayer(rotationZ=arrowRotation))}}
    if(expanded)Popup(popupPositionProvider=object:PopupPositionProvider{override fun calculatePosition(anchorBounds:androidx.compose.ui.unit.IntRect,windowSize:IntSize,layoutDirection:LayoutDirection,popupContentSize:IntSize)=IntOffset(0,anchorBounds.bottom+marketBarGapPx)},onDismissRequest={closePanel()},properties=PopupProperties(focusable=true,dismissOnBackPress=true,dismissOnClickOutside=true)){
      // wrapContentHeight مهم است: fillMaxSize باعث می‌شد Popup برای جا شدن در صفحه
      // به بالای anchor هل داده شود و دکمه «نبض بازار» را بپوشاند.
      Box(Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal=8.dp),contentAlignment=Alignment.TopCenter){
       AnimatedVisibility(panelVisible,enter=expandVertically(expandFrom=Alignment.Top,animationSpec=tween(620))+fadeIn(tween(500)),exit=shrinkVertically(shrinkTowards=Alignment.Top,animationSpec=tween(420))+fadeOut(tween(350))){
        Box(Modifier.fillMaxWidth(.86f).shadow(14.dp,RoundedCornerShape(25.dp),ambientColor=Color(0xFFFF4EA3).copy(.20f),spotColor=Color(0xFF45E4E0).copy(.18f)).clip(RoundedCornerShape(25.dp)).background(panelBase.copy(alpha=if(darkMarket).97f else .98f)).background(Brush.linearGradient(listOf(Color(0x12FF3D91),Color.Transparent,Color(0x124DE8E2)))).border(1.3.dp,Brush.linearGradient(listOf(Color(0xFFFF4EA3),Color(0xFF813C79),Color(0xFF45E4E0))),RoundedCornerShape(25.dp)).clickable{}.padding(horizontal=11.dp,vertical=7.dp)){
          Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
           ModernMarketEntryButton(dark=darkMarket,onClick={expanded=false;onOpenMarket()})

           if(loading)LinearProgressIndicator(Modifier.fillMaxWidth(.72f),color=Color(0xFF55D9D5),trackColor=muted.copy(.10f))
           CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){PremiumMarketTile("دلار آزاد","هر دلار",dollar,previousDollar,unit,Color(0xFF35BDB9),"\$",Modifier.weight(1f),darkMarket);PremiumMarketTile("طلای ۱۸ عیار","هر گرم",gold,previousGold,unit,Color(0xFFD4A526),"Au",Modifier.weight(1f),darkMarket)}}
           if(failed)Text(if(gold!=null||dollar!=null)"دسترسی تازه ممکن نشد؛ آخرین قیمت ذخیره‌شده نمایش داده می‌شود." else "دریافت نرخ‌ها ممکن نشد؛ اینترنت را بررسی کنید.",color=Color(0xFFFF506C),style=MaterialTheme.typography.bodySmall)
           Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){Icon(Icons.Filled.Refresh,"به‌روزرسانی",tint=if(stale)Color(0xFFFF506C)else muted,modifier=Modifier.size(20.dp).clip(CircleShape).clickable(enabled=!loading){refreshKey++}.padding(2.dp));Text("آخرین بروزرسانی: ${updatedAt?.let{PersianDate.formatDateTime(it)}?:"—"}",color=if(stale)Color(0xFFFF506C)else muted,style=MaterialTheme.typography.labelSmall)}
          }
         }
        }
       }
      }
    }

@Composable
private fun ModernMarketEntryButton(dark:Boolean,onClick:()->Unit){
 val shape=RoundedCornerShape(18.dp)
 val deep=if(dark)Color(0xFF111827) else Color(0xFFF5F7FF)
 val title=if(dark)Color.White else Color(0xFF182034)
 Box(
  Modifier.fillMaxWidth().height(58.dp)
   .shadow(9.dp,shape,ambientColor=Color(0xFF4DE4DE).copy(.22f),spotColor=Color(0xFFFF5BA8).copy(.18f))
   .clip(shape)
   .background(Brush.horizontalGradient(listOf(Color(0xFF0E6F78).copy(if(dark).72f else .18f),deep,Color(0xFF8B285F).copy(if(dark).62f else .14f))))
   .border(1.dp,Brush.horizontalGradient(listOf(Color(0xFF55E7DF).copy(.78f),Color.White.copy(.12f),Color(0xFFFF6EAE).copy(.72f))),shape)
   .clickable(onClick=onClick)
   .padding(horizontal=10.dp),
  contentAlignment=Alignment.Center
 ){
  // لکه‌های نور ثابت، بدون انیمیشن دائمی و مصرف باتری اضافه.
  Box(Modifier.align(Alignment.CenterStart).size(72.dp).background(Brush.radialGradient(listOf(Color(0xFF55E7DF).copy(.13f),Color.Transparent)),CircleShape))
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
    Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFF42D8D2),Color(0xFF186E86)))).border(1.dp,Color.White.copy(.34f),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){
     Icon(Icons.Filled.QueryStats,"ورود به صفحه نبض بازار",tint=Color.White,modifier=Modifier.size(23.dp))
    }
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(1.dp)){
     Text("مشاهده کامل نبض بازار",color=title,fontWeight=FontWeight.Black,style=MaterialTheme.typography.bodyMedium,maxLines=1)
     Text("طلا، سکه و ارز در یک نگاه",color=title.copy(.65f),style=MaterialTheme.typography.labelSmall,maxLines=1)
    }
    Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFF5EA8).copy(if(dark).20f else .13f)).border(1.dp,Color(0xFFFF77B5).copy(.50f),CircleShape),contentAlignment=Alignment.Center){
     Icon(Icons.Filled.ArrowOutward,null,tint=Color(0xFFFF77B5),modifier=Modifier.size(17.dp))
    }
   }
  }
 }
}

internal fun marketChangePercent(value:Long?,previous:Long?):Double?=if(value!=null&&previous!=null&&previous>0)(value.toDouble()-previous.toDouble())*100.0/previous.toDouble() else null
internal fun marketChangeLabel(change:Double?):String?=when{change==null->null;change==0.0->"بدون تغییر";else->{val decimals=if(kotlin.math.abs(change)<0.01)4 else 2;val number=String.format(java.util.Locale.US,"%.${decimals}f",kotlin.math.abs(change));Digits.toPersian((if(change>0)"↗  +" else "↘  −")+number+"٪")}}
@Composable private fun PremiumMarketTile(title:String,subtitle:String,value:Long?,previous:Long?,unit:ir.kharjyar.app.core.money.MoneyUnit,accent:Color,symbol:String,modifier:Modifier=Modifier,dark:Boolean=true){
 val change=marketChangePercent(value,previous);val positive=change!=null&&change>0;val negative=change!=null&&change<0;val trend=when{positive->Color(0xFF28B965);negative->Color(0xFFE94361);else->Color(0xFF7E8796)};val tile=if(dark)Color(0xFF101722)else Color(0xFFFFFFFF);val main=if(dark)Color(0xFFF6F2F6)else Color(0xFF202633);val secondary=if(dark)Color(0xFF9CA3AF)else Color(0xFF697181)
 Surface(modifier=modifier,shape=RoundedCornerShape(16.dp),color=tile.copy(alpha=if(dark).98f else 1f),border=BorderStroke(1.3.dp,accent.copy(alpha=.58f)),shadowElevation=3.dp){Column(Modifier.background(Brush.verticalGradient(listOf(accent.copy(.08f),Color.Transparent))).padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
  Box(Modifier.size(39.dp).shadow(6.dp,CircleShape,ambientColor=accent.copy(.5f),spotColor=accent.copy(.4f)).background(Brush.radialGradient(listOf(accent.copy(.90f),accent.copy(.38f),if(dark)Color(0xFF18222E)else Color(0xFFE9EEF4))),CircleShape).border(2.dp,accent.copy(.62f),CircleShape),contentAlignment=Alignment.Center){Text(symbol,color=if(symbol=="Au")Color(0xFFFFDF72)else Color(0xFFE5FFFF),fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleSmall)}
  Text(title,color=main,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center);Text(subtitle,color=secondary,style=MaterialTheme.typography.labelSmall,maxLines=1)
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){HorizontalDivider(Modifier.weight(1f),color=secondary.copy(.16f));Box(Modifier.padding(horizontal=6.dp).size(6.dp).background(accent,CircleShape));HorizontalDivider(Modifier.weight(1f),color=secondary.copy(.16f))}
  Text(value?.let{Money.format(it,unit)}?:"—",color=accent,fontWeight=FontWeight.Black,style=MaterialTheme.typography.bodyMedium,maxLines=1)
  Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(9.dp),color=trend.copy(alpha=.09f),border=BorderStroke(1.dp,trend.copy(alpha=.28f))){Column(Modifier.padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(marketChangeLabel(change)?:"نرخ لحظه‌ای",color=trend,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium);Text(if(change==null)"دریافت آنلاین" else "از بروزرسانی قبل",color=trend.copy(.78f),style=MaterialTheme.typography.labelSmall)}}
 }}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, nav: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recent by viewModel.scopedRecent.collectAsState()
    val reviewCount by viewModel.reviewCount.collectAsState()
    val allTx by viewModel.allTransactions.collectAsState()
    val bankBalances by viewModel.bankBalances.collectAsState()
    val scopedTx by viewModel.scopedTransactions.collectAsState()
    val defaultAccount by viewModel.defaultAccount.collectAsState()
    val skin = LocalAppSkin.current
    // از تنظیمات خوانده می‌شود تا با رفتن به صفحه دیگر و برگشتن حفظ شود
    val amountVisible = settings.amountsVisible
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var pendingBalanceAccount by remember { mutableStateOf<ir.kharjyar.app.data.db.AccountEntity?>(null) }
    var greenBalanceAccounts by remember { mutableStateOf(setOf<Long>()) }
    fun timedMessage(message:String){
        scope.launch { snackbar.currentSnackbarData?.dismiss();launch{snackbar.showSnackbar(message,duration=SnackbarDuration.Indefinite)};kotlinx.coroutines.delay(4_000);snackbar.currentSnackbarData?.dismiss() }
    }
    fun refreshBankBalance(account:ir.kharjyar.app.data.db.AccountEntity){
        scope.launch {
            val sms=ir.kharjyar.app.core.sms.BankBalanceRefreshService.latest(context,account,accounts.filter{!it.archived&&ir.kharjyar.app.core.sms.BankSenderResolver.sameBank(account.bankName,it.bankName)},viewModel.repo.accountDao.allSenders())
            if(sms==null){timedMessage("پیام بانکی منطبق با شماره این حساب پیدا نشد");return@launch}
            viewModel.repo.db.bankBalanceSnapshotDao().upsert(ir.kharjyar.app.data.db.BankBalanceSnapshotEntity(account.id,sms.balanceRial,sms.occurredAt,sms.smsId))
            val estimated=ir.kharjyar.app.core.balance.AccountBalance.estimate(account,viewModel.repo.txDao.allOnce()).rial
            if(estimated!=null&&estimated==sms.balanceRial){
                greenBalanceAccounts=greenBalanceAccounts+account.id;timedMessage("موجودی بروز شد")
                launch{kotlinx.coroutines.delay(30_000);greenBalanceAccounts=greenBalanceAccounts-account.id}
            }else timedMessage("موجودی مغایرت دارد")
        }
    }
    val smsPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->pendingBalanceAccount?.let{if(granted)refreshBankBalance(it)else timedMessage("برای بروزرسانی موجودی، دسترسی پیامک لازم است")};pendingBalanceAccount=null}
    fun requestBalanceRefresh(account:ir.kharjyar.app.data.db.AccountEntity){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED)refreshBankBalance(account)
        else{pendingBalanceAccount=account;smsPermission.launch(Manifest.permission.READ_SMS)}
    }
    // فیلتر فهرست «تراکنش‌های اخیر» با زدن چیپ واریز/برداشت روی کارت‌ها
    // ۰ = همه، ۱ = فقط واریزها، ۲ = فقط برداشت‌ها
    var recentFilter by remember { mutableStateOf(0) }
    var pendingDelete by remember { mutableStateOf<ir.kharjyar.app.data.db.TransactionEntity?>(null) }

    /** حذف تراکنش از فهرست «اخیر» با امکان بازگرداندن. */
    fun deleteWithUndo(tx: ir.kharjyar.app.data.db.TransactionEntity) {
        scope.launch {
            viewModel.repo.txDao.delete(tx.id)
            val res = snackbar.showSnackbar(
                message = "تراکنش حذف شد",
                actionLabel = "بازگرداندن",
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) viewModel.repo.txDao.restore(listOf(tx))
        }
    }

    pendingDelete?.let { tx ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("حذف تراکنش") }, text = { Text("آیا از حذف این تراکنش مطمئن هستید؟") }, confirmButton = { TextButton({ pendingDelete = null; deleteWithUndo(tx) }) { Text("حذف", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton({ pendingDelete = null }) { Text("انصراف") } })
    }

    Scaffold(
        containerColor = Color.Transparent,
        // پیام‌های کوتاه (مثل «شماره کارت کپی شد») بالاتر از دکمه گرد ثبت
        // تراکنش می‌نشینند تا پشت آن پنهان نشوند.
        snackbarHost = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                GlassSnackbarHost(snackbar)
            }
        },
        modifier = Modifier.imePadding(),
        // نوار بالا/پایین سیستم یک‌بار در AppRoot اعمال شده؛ تکرارش اینجا باعث
        // حاشیه مرده در بالای صفحه و بالای دکمه‌های پایین می‌شد.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        // انیمیشن ورود فقط برای نخستین نمایش صفحه؛ ردیف‌هایی که حین اسکرول
        // ساخته می‌شوند بدون تأخیر ظاهر می‌شوند.
        ScreenEnterAnimation {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // کلیک روی هر جای صفحه به‌جز خود ردیف‌ها و چیپ‌ها (که کلیک را
                // مصرف می‌کنند)، فیلتر واریز/برداشت را برمی‌دارد
                .pointerInput(recentFilter) {
                    if (recentFilter != 0) {
                        detectTapGestures { recentFilter = 0 }
                    }
                },
            // بدون فاصله مرده: کارت اصلی درست زیر نوار بالایی و فهرست تا خط نوار پایین
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- کیف پول: خلاصه ماه و کارت‌های بانکی در یک نوار ----------
            // قبلاً «کارت خلاصه» و «ردیف حساب‌ها» دو بخش جدا و زیر هم بودند و صفحه
            // را شلوغ می‌کردند. حالا یک نوار افقی است: صفحه نخست خلاصه همه حساب‌ها،
            // و بعد از آن هر حساب یک کارت با خلاصه واریز/برداشت خودش.
            item {
                val order=settings.dashboardAccountOrder
                val active = accounts.filter { !it.archived }.sortedBy { a -> order.indexOf(a.id).let { if(it<0) Int.MAX_VALUE else it } }
                // با تغییر خودکار عنوان ماه در نیمه‌شب، بازه کارت‌های حساب نیز نوسازی می‌شود.
                val monthRange = remember(summary.monthTitle) { viewModel.repo.currentPersianMonthRange() }
                val wholeRange = summary.range == AppViewModel.SummaryRange.ALL
                // خلاصه هر حساب (یا همه حساب‌ها) با همان بازه‌ای که کاربر انتخاب کرده
                fun rangeSummary(accountId: Long?) =
                    if (wholeRange) TxSummarizer.summarize(allTx, accountId)
                    else TxSummarizer.summarize(allTx, accountId, monthRange.first, monthRange.second)

                // چرخ‌فلک کارت‌ها: با هر کشیدن انگشت دقیقاً یک کارت وسط صفحه می‌ایستد
                // (پهنای هر صفحه = عرض فهرست منهای دو لبه، و چسبیدن با snap).
                val rowState = rememberLazyListState()
                val snapFling = rememberSnapFlingBehavior(lazyListState = rowState)
                // سرعت پرتاب محدود می‌شود تا حتی با کشیدن محکم، دو یا سه کارت
                // یک‌جا رد نشود و کنترل همیشه نزدیک به یک کارت بماند.
                val controlledFling = remember(snapFling) {
                    object : androidx.compose.foundation.gestures.FlingBehavior {
                        override suspend fun androidx.compose.foundation.gestures.ScrollScope.performFling(initialVelocity: Float): Float =
                            with(snapFling) { performFling(initialVelocity.coerceIn(-1100f, 1100f)) }
                    }
                }
                val peek = 22.dp
                val pageWidth = (LocalConfiguration.current.screenWidthDp.dp - 32.dp - peek * 2)
                    .coerceAtLeast(180.dp)
                // ردیف در حالت عادی هم‌ارتفاع کارت‌های جمع‌وجور است؛ فقط با انتخاب
                // یک حساب برای نمایش جزئیات باز می‌شود و تراکنش‌های اخیر بی‌دلیل پایین نمی‌روند.
                val compactAccountCardHeight = 230.dp
                val accountCarouselHeight = 240.dp
                val dashboardLayoutDirection = LocalLayoutDirection.current
                // وضعیت جابه‌جایی تا زمان رهاکردن ثابت می‌ماند؛ ترتیب فقط در پایان ذخیره می‌شود.
                var draggingId by remember { mutableStateOf<Long?>(null) }
                var dragOriginIndex by remember { mutableStateOf(-1) }
                var dragTargetIndex by remember { mutableStateOf(-1) }
                var floatingX by remember { mutableStateOf(0f) }
                var floatingY by remember { mutableStateOf(0f) }
                var floatingOriginX by remember { mutableStateOf(0f) }
                var floatingOriginY by remember { mutableStateOf(0f) }
                var hoverTargetIndex by remember { mutableStateOf(-1) }
                var reorderStepJob by remember { mutableStateOf<Job?>(null) }
                val edgeThresholdPx=with(LocalDensity.current){52.dp.toPx()}
                val screenWidthPx=with(LocalDensity.current){LocalConfiguration.current.screenWidthDp.dp.toPx()}
                // مختصات فیزیکی slot هر کارت؛ محاسبه مقصد بر پایه موقعیت واقعی است،
                // بنابراین در RTL و LTR و با عبور از چند کارت یکسان رفتار می‌کند.
                val accountCardX = remember { mutableMapOf<Long, Float>() }
                val accountCardWidth = remember { mutableMapOf<Long, Float>() }

                EnterCard(0) {
                    LazyRow(
                        modifier = Modifier.height(accountCarouselHeight).animateContentSize(animationSpec=tween(320)),
                        state = rowState,
                        flingBehavior = controlledFling,
                        contentPadding = PaddingValues(horizontal = peek),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ----- صفحه نخست: خلاصه همه حساب‌ها -----
                        item {
                            val total = rangeSummary(null)
                            val totalRial = active.sumOf { acc -> AccountBalance.estimate(acc, allTx).rial ?: 0L }
                            HeroCard(
                                modifier = Modifier
                                    .width(pageWidth)
                                    .height(compactAccountCardHeight)
                                    .clickable { scope.launch { viewModel.settingsRepo.setDefaultAccount(null) } },
                                neon = settings.cardShine
                            ) {
                                // بدنه کارت اصلی دقیقاً تمام ابعاد همان قاب ۲۳۰dp کارت‌های حساب را
                                // پر می‌کند؛ HeroCard در حالت wrap-content در بعضی تم‌ها کوتاه‌تر دیده می‌شد.
                                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // عنوان، خودش کلید تغییر بازه است: «این ماه ⇄ همه»
                                        Row(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.toggleSummaryRange() }
                                                .padding(end = 6.dp, top = 2.dp, bottom = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            EmbossedText(
                                                "خلاصه ${summary.monthTitle}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = skin.onHero,
                                                maxLines = 1
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.SwapHoriz,
                                                contentDescription = "تغییر بازه",
                                                tint = skin.onHero.copy(alpha = 0.85f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        // نشان «همه حساب‌ها»: با زدنش فیلتر حساب برداشته می‌شود
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(heroChipBg(skin))
                                                .border(1.dp, skin.onHero.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (defaultAccount == null) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = skin.onHero,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(
                                                "همه حساب‌ها",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = skin.onHero,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // دکمه چشم برای پنهان/نمایش مبلغ
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(heroChipBg(skin))
                                                .border(1.dp, skin.onHero.copy(alpha = 0.28f), CircleShape)
                                                .clickable { scope.launch { viewModel.settingsRepo.setAmountsVisible(!amountVisible) } },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (amountVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (amountVisible) "پنهان کردن مبلغ" else "نمایش مبلغ",
                                                tint = skin.onHero,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            EmbossedText(
                                                if (amountVisible) Money.format(total.netRial, settings.moneyUnit) else "••••••••",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = skin.onHero,
                                                maxLines = 1,
                                                depth = 1.25f
                                            )
                                            Text(
                                                if (summary.range == AppViewModel.SummaryRange.MONTH) "خالص این ماه" else "خالص همه تراکنش‌ها",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = skin.onHero.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                    // مجموع مانده حساب‌ها، همان چیزی که پیش‌تر کارت جدا داشت
                                    if (active.isNotEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            "مجموع مانده برآوردی ${Digits.toPersian(active.size.toString())} حساب: " +
                                                (if (amountVisible) Money.format(totalRial, settings.moneyUnit) else "••••••"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.9f),
                                            maxLines = 1
                                        )
                                    }
                                    if (summary.hasDataOutsideRange) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "در این ماه تراکنشی نیست؛ برای دیدن همه، روی عنوان بزنید.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.85f)
                                        )
                                    }
                                    if (total.pendingCount > 0) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "به‌جز ${Digits.toPersian(total.pendingCount.toString())} مورد تأییدنشده (در جمع بالا حساب نشده)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = skin.onHero.copy(alpha = 0.8f)
                                        )
                                    }
                                    // دقیقاً مانند کارت‌های حساب، دو نوار هم‌اندازه به پایین کارت می‌چسبند.
                                    Spacer(Modifier.weight(1f))
                                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                        CompactAccountFlow("واریز",if(amountVisible)Money.format(total.incomeRial,settings.moneyUnit)else "••••",skin.incomeColor,Icons.Filled.SouthWest,Modifier.weight(1f))
                                        CompactAccountFlow("برداشت",if(amountVisible)Money.format(total.expenseRial,settings.moneyUnit)else "••••",skin.expenseColor,Icons.Filled.NorthEast,Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // ----- صفحه‌های بعدی: هر حساب یک کارت، با خلاصه خودش -----
                        items(active.size, key = { active[it].id }) { idx ->
                            val account = active[idx]
                            val isDragging = draggingId == account.id
                            // پیش‌نمایش زنده ترتیب: کارت درگ‌شده از آرایه برداشته و در مقصد
                            // موقت قرار می‌گیرد؛ هر کارت زیر مسیر فوراً به slot خالی قبلی می‌لغزد.
                            val previewIds = active.map { it.id }.toMutableList().also { ids ->
                                if (draggingId != null && dragOriginIndex in ids.indices && dragTargetIndex in ids.indices) {
                                    ids.removeAt(dragOriginIndex)
                                    ids.add(dragTargetIndex, draggingId!!)
                                }
                            }
                            val previewIndex = previewIds.indexOf(account.id)
                            val targetSlotId = active.getOrNull(previewIndex)?.id
                            val liveShiftPx = if (draggingId == null || isDragging || targetSlotId == null) 0f
                                else (accountCardX[targetSlotId] ?: 0f) - (accountCardX[account.id] ?: 0f)
                            val neighborOffset by animateFloatAsState(
                                targetValue = liveShiftPx,
                                animationSpec = tween(durationMillis = 560),
                                label = "neighborCardSlowShift"
                            )
                            val est = AccountBalance.estimate(account, allTx)
                            val accSum = rangeSummary(account.id)
                            val bankSnapshot = bankBalances.firstOrNull { it.accountId == account.id }
                            val hasDiscrepancy = est.rial != null && bankSnapshot != null && est.rial != bankSnapshot.balanceRial
                            // رنگ متن روی کارت، مثل خود BankCard از روشنایی رنگ حساب می‌آید
                            val onCard = if (account.accountType == ir.kharjyar.app.data.db.AccountType.CASH) Color.White else if (Color(account.colorArgb).luminance() > 0.55f) Color(0xFF14121A) else Color.White
                            // مختصات هر کارت مستقل نگه داشته می‌شود؛ یک مختصات مشترک بین
                            // کارت‌ها باعث می‌شد کارت شناور هنگام شروع در محل کارت دیگری ظاهر شود.
                            var cardWindowPosition by remember(account.id) { mutableStateOf(Offset.Zero) }
                            BankCard(
                                title = account.title,
                                bankName = account.bankName,
                                colorArgb = account.colorArgb,
                                accountType = account.accountType,
                                ownerName = account.ownerName,
                                cashLocation = account.cashLocation,
                                balanceText = when {
                                    !amountVisible -> "••••••"
                                    est.rial != null -> Money.format(est.rial, settings.moneyUnit)
                                    else -> "—"
                                },
                                balanceHint = "مانده برآوردی",
                                selected = defaultAccount?.id == account.id,
                                cardNumber = account.cardNumber,
                                accountNumber = account.accountNumber,
                                iban = account.iban,
                                expiry = account.cardExpiry,
                                cvv2 = account.cardCvv2,
                                showSecrets = amountVisible,
                                balanceColor = when { account.id in greenBalanceAccounts -> Color(0xFF43E08D); hasDiscrepancy -> skin.expenseColor; else -> null },
                                showDetailsWhenSelected = false,
                                modifier = Modifier
                                    .width(pageWidth)
                                    .height(compactAccountCardHeight)
                                    .animateItemPlacement(animationSpec = tween(durationMillis = 480))
                                    .onGloballyPositioned { coordinates ->
                                        val physical = coordinates.positionInWindow()
                                        accountCardX[account.id] = physical.x
                                        accountCardWidth[account.id] = coordinates.size.width.toFloat()
                                        if (!isDragging) cardWindowPosition = physical
                                    }
                                    .graphicsLayer {
                                        translationX = neighborOffset
                                        alpha = if (isDragging) 0f else 1f
                                    }
                                    .pointerInput(account.id, active.map { it.id }) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                floatingOriginX = cardWindowPosition.x
                                                floatingOriginY = cardWindowPosition.y
                                                draggingId = account.id
                                                dragOriginIndex = idx
                                                dragTargetIndex = idx
                                                floatingX = 0f
                                                floatingY = 0f
                                                hoverTargetIndex=idx
                                                reorderStepJob?.cancel();reorderStepJob=null
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                floatingX += amount.x
                                                floatingY += amount.y
                                                // مرکز کارت شناور با مرکز همه slotهای واقعی مقایسه می‌شود.
                                                // نزدیک‌ترین slot مقصد است؛ در یک حرکت می‌توان از هر تعداد
                                                // کارت عبور کرد و جهت فیزیکی چپ/راست وابسته به RTL نیست.
                                                val draggedCenter = floatingOriginX + floatingX +
                                                    (accountCardWidth[account.id] ?: size.width.toFloat()) / 2f
                                                val physicalStep = size.width.toFloat() + 10.dp.toPx()
                                                val indexDirection = if (dashboardLayoutDirection == LayoutDirection.Rtl) -1f else 1f
                                                val nearestTarget = active.indices.minByOrNull { candidate ->
                                                    val candidateId = active[candidate].id
                                                    // کارت‌های خارج viewport هنوز compose نشده‌اند؛ مرکز slot آن‌ها
                                                    // از فاصله ثابت صفحات extrapolate می‌شود تا عبور چندکارتی ممکن باشد.
                                                    val fallbackLeft = floatingOriginX +
                                                        (candidate - dragOriginIndex) * physicalStep * indexDirection
                                                    val center = (accountCardX[candidateId] ?: fallbackLeft) +
                                                        (accountCardWidth[candidateId] ?: size.width.toFloat()) / 2f
                                                    kotlin.math.abs(center - draggedCenter)
                                                } ?: dragOriginIndex
                                                val physicalEdge=when{draggedCenter<edgeThresholdPx->-1;draggedCenter>screenWidthPx-edgeThresholdPx->1;else->0}
                                                val logicalEdge=physicalEdge*(if(dashboardLayoutDirection==LayoutDirection.Rtl)-1 else 1)
                                                val desiredTarget=when{logicalEdge<0->active.indices.first;logicalEdge>0->active.indices.last;else->nearestTarget}

                                                // مقصد فقط به‌صورت یک خانه در هر مرحله تغییر می‌کند. بین دو مرحله
                                                // ۶۸۰ms مکث داریم تا کارت کناری فرصت کند طی انیمیشن نرم جای خالی را
                                                // پر کند و کاربر بتواند همان‌جا رها کند یا مسیر را برگرداند.
                                                if(desiredTarget!=hoverTargetIndex){
                                                    hoverTargetIndex=desiredTarget
                                                    reorderStepJob?.cancel();reorderStepJob=null
                                                    if(desiredTarget!=dragTargetIndex) reorderStepJob=scope.launch{
                                                        while(draggingId==account.id&&dragTargetIndex!=hoverTargetIndex){
                                                            delay(680)
                                                            if(draggingId!=account.id) break
                                                            val step=if(hoverTargetIndex>dragTargetIndex)1 else -1
                                                            dragTargetIndex=(dragTargetIndex+step).coerceIn(active.indices)
                                                            // حین نگه‌داشتن LazyRow را اسکرول نمی‌کنیم؛ خارج‌شدن
                                                            // آیتم مبدأ از viewport، pointer را cancel و کارت را خودکار رها می‌کرد.
                                                        }
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                if (dragOriginIndex >= 0 && dragTargetIndex >= 0 && dragOriginIndex != dragTargetIndex) {
                                                    val ids = active.map { it.id }.toMutableList()
                                                    ids.removeAt(dragOriginIndex)
                                                    ids.add(dragTargetIndex, account.id)
                                                    val settledIndex=dragTargetIndex
                                                    scope.launch {
                                                        viewModel.settingsRepo.setDashboardAccountOrder(ids)
                                                        // پیمایش فقط بعد از برداشتن انگشت انجام می‌شود؛ بنابراین
                                                        // gesture تا آخر در اختیار کاربر می‌ماند.
                                                        rowState.animateScrollToItem(settledIndex+1)
                                                    }
                                                }
                                                reorderStepJob?.cancel();reorderStepJob=null;hoverTargetIndex=-1
                                                draggingId = null; floatingX = 0f; floatingY = 0f
                                            },
                                            onDragCancel = { reorderStepJob?.cancel();reorderStepJob=null;hoverTargetIndex=-1;draggingId = null; floatingX = 0f; floatingY = 0f }
                                        )
                                    },
                                onClick = {
                                    // انتخاب کارت = تغییر حساب پیش‌فرض داشبورد
                                    // و برداشتن فیلتر واریز/برداشت
                                    recentFilter = 0
                                    scope.launch {
                                        viewModel.settingsRepo.setDefaultAccount(
                                            if (defaultAccount?.id == account.id) null else account.id
                                        )
                                    }
                                },
                                onCopy = { label, text ->
                                    clipboard.setText(AnnotatedString(text))
                                    scope.launch {
                                        // پیام کوتاه تأیید؛ خودش بعد از چند ثانیه محو می‌شود
                                        snackbar.currentSnackbarData?.dismiss()
                                        snackbar.showSnackbar(
                                            message = "$label کپی شد",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ) {
                                AccountBelowCardPanel(
                                    income = if (amountVisible) Money.format(accSum.incomeRial, settings.moneyUnit) else "••••",
                                    expense = if (amountVisible) Money.format(accSum.expenseRial, settings.moneyUnit) else "••••",
                                    incomeColor = skin.incomeColor,
                                    expenseColor = skin.expenseColor,
                                    bankStatus = bankSnapshot?.let { snap ->
                                        "مانده پیامک: " + (if(amountVisible) Money.format(snap.balanceRial,settings.moneyUnit) else "••••••") +
                                            (if(hasDiscrepancy && est.rial!=null) " • مغایرت ${Money.format(kotlin.math.abs(est.rial-snap.balanceRial),settings.moneyUnit)}" else "")
                                    } ?: "مانده پیامک: ثبت نشده",
                                    hasDiscrepancy = hasDiscrepancy,
                                    onDiscrepancy = { nav.navigate("reviewImport?accountId=${account.id}") },
                                    onRefresh = { requestBalanceRefresh(account) }
                                )
                            }
                            if (isDragging) {
                                Popup(
                                    // offset در overload مبتنی بر Alignment داخل RTL دوباره آینه می‌شود.
                                    // PositionProvider مختصات فیزیکی پنجره را مستقیماً برمی‌گرداند تا
                                    // حرکت چپ/راست کارت دقیقاً هم‌جهت حرکت انگشت باقی بماند.
                                    popupPositionProvider = object : PopupPositionProvider {
                                        override fun calculatePosition(
                                            anchorBounds: androidx.compose.ui.unit.IntRect,
                                            windowSize: IntSize,
                                            layoutDirection: LayoutDirection,
                                            popupContentSize: IntSize
                                        ): IntOffset = IntOffset(
                                            (floatingOriginX + floatingX).roundToInt(),
                                            (floatingOriginY + floatingY).roundToInt()
                                        )
                                    },
                                    properties = PopupProperties(focusable = false, clippingEnabled = false)
                                ) {
                                    BankCard(
                                        account = account,
                                        balanceText = when {
                                            !amountVisible -> "••••••"
                                            est.rial != null -> Money.format(est.rial, settings.moneyUnit)
                                            else -> "—"
                                        },
                                        balanceCaption = "مانده برآوردی",
                                        selected = defaultAccount?.id == account.id,
                                        masked = !amountVisible,
                                        showDetailsWhenSelected = false,
                                        modifier = Modifier.width(pageWidth).height(compactAccountCardHeight).graphicsLayer {
                                            // اندازه در شروع Drag تغییر نمی‌کند تا نقطه‌ای که کاربر
                                            // گرفته دقیقاً زیر همان نقطه انگشت باقی بماند.
                                            scaleX = 1f
                                            scaleY = 1f
                                            // شکل سایه دقیقاً با گوشه‌های گرد کارت یکی است؛ رنگ کم‌غلظت
                                            // و ارتفاع بیشتر، لبه خطی را به هاله نرم تبدیل می‌کند.
                                            shape = RoundedCornerShape(22.dp)
                                            clip = false
                                            shadowElevation = 34.dp.toPx()
                                            ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                                            spotShadowColor = Color.Black.copy(alpha = 0.28f)
                                        }
                                    )
                                }
                            }
                        }

                        // ----- آخرین صفحه: افزودن حساب -----
                        item {
                            SkinCard(
                                modifier = Modifier
                                    .width(if (active.isEmpty()) pageWidth else pageWidth * 0.55f)
                                    .height(compactAccountCardHeight)
                                    .clickable { nav.navigate("accountEdit/0") }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(skin.accent.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = skin.accent)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        if (active.isEmpty()) "هنوز حسابی معرفی نکرده‌اید — افزودن حساب" else "افزودن حساب",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = skin.onBackdrop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------- میان‌بر «ثبت سریع» ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(skin.accent.copy(alpha = 0.22f), skin.accent.copy(alpha = 0.06f))
                            )
                        )
                        .border(1.dp, skin.accent.copy(alpha = 0.38f), RoundedCornerShape(30.dp))
                        .clickable { nav.navigate("quickAdd") }
                        .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(skin.accent.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            tint = skin.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "ثبت سریع",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = skin.onBackdrop,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = skin.accent.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ---------- نیازمند بررسی ----------
            if (reviewCount > 0) {
                item {
                    EnterCard(1) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("review") }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.RateReview, null, tint = skin.expenseColor)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${Digits.toPersian(reviewCount.toString())} مورد نیازمند بررسی",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = skin.onBackdrop
                                )
                            }
                        }
                    }
                }
            }

            // ---------- تراکنش‌های اخیر ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        when (recentFilter) {
                            1 -> "واریزهای اخیر"
                            2 -> "برداشت‌های اخیر"
                            else -> "تراکنش‌های اخیر"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = skin.onBackdrop
                    )
                    // با فیلتر فعال، راه برگشت به همه تراکنش‌ها
                    if (recentFilter != 0) {
                        Text(
                            "نمایش همه",
                            style = MaterialTheme.typography.labelLarge,
                            color = skin.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { recentFilter = 0 }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            val shownRecent = when (recentFilter) {
                1 -> recent.filter { it.direction == ir.kharjyar.app.data.db.TxDirection.DEPOSIT }
                2 -> recent.filter { it.direction == ir.kharjyar.app.data.db.TxDirection.WITHDRAW }
                else -> recent
            }
            if (shownRecent.isEmpty()) {
                item {
                    EmptyState(
                        when (recentFilter) {
                            1 -> "واریزی ثبت نشده"
                            2 -> "برداشتی ثبت نشده"
                            else -> "تراکنشی ثبت نشده"
                        },
                        if (recentFilter == 0) "از دکمه «ثبت تراکنش» شروع کنید یا منتظر پیامک بانکی بمانید"
                        else "برای دیدن بقیه تراکنش‌ها «نمایش همه» را بزنید"
                    )
                }
            } else {
                items(shownRecent.size) { idx ->
                    val tx = shownRecent[idx]
                    EnterCard(4 + idx) {
                        SwipeActionRow(
                            onDelete = { pendingDelete = tx },
                            onEdit = { nav.navigate("tx/${tx.id}") }
                        ) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("tx/${tx.id}") }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DirectionBadge(tx.direction, tx.nature)
                                        if (tx.status == TxStatus.PENDING) {
                                            Text(
                                                "در انتظار تأیید",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    Text(
                                        tx.description.ifBlank { tx.counterparty.ifBlank { "بدون توضیح" } },
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        color = skin.onBackdrop
                                    )
                                    Text(
                                        PersianDate.formatDateTime(tx.occurredAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = skin.onBackdrop.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    Money.format(tx.amountRial, settings.moneyUnit),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = skin.onBackdrop
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
        }
    }
}

/**
 * پس‌زمینه چیپ‌های روی کارت شاخص.
 * روی تم‌های تیره یک لایه مشکی و روی تم روشن یک لایه سفید می‌نشیند تا
 * متن در هر دو حالت کنتراست کافی داشته باشد.
 */
private fun heroChipBg(skin: ir.kharjyar.app.ui.theme.AppSkin): Color =
    if (skin.dark) Color.Black.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.72f)

@Composable
private fun AccountBelowCardPanel(
    income:String, expense:String, incomeColor:Color, expenseColor:Color,
    bankStatus:String?, hasDiscrepancy:Boolean, onDiscrepancy:()->Unit, onRefresh:()->Unit
) {
    Column(Modifier.fillMaxWidth().padding(top=5.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            bankStatus?.let { status -> Text(status,style=MaterialTheme.typography.labelSmall,color=if(hasDiscrepancy)Color(0xFFFF8A93)else Color.White.copy(.78f),maxLines=1,modifier=Modifier.weight(1f).clickable(enabled=hasDiscrepancy,onClick=onDiscrepancy)) } ?: Spacer(Modifier.weight(1f))
            TextButton(onClick=onRefresh,contentPadding=PaddingValues(horizontal=6.dp,vertical=0.dp)){Icon(Icons.Filled.Refresh,null,Modifier.size(14.dp),tint=Color.White);Spacer(Modifier.width(3.dp));Text("بروزرسانی",style=MaterialTheme.typography.labelSmall,color=Color.White)}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            CompactAccountFlow("واریز",income,incomeColor,Icons.Filled.SouthWest,Modifier.weight(1f))
            CompactAccountFlow("برداشت",expense,expenseColor,Icons.Filled.NorthEast,Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactAccountFlow(label:String,value:String,tint:Color,icon:ImageVector,modifier:Modifier=Modifier){
    Row(modifier.background(Color.Black.copy(alpha=.32f),RoundedCornerShape(10.dp)).border(1.dp,Color.White.copy(alpha=.16f),RoundedCornerShape(10.dp)).padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){
        Icon(icon,null,tint=tint,modifier=Modifier.size(15.dp))
        Text(label,style=MaterialTheme.typography.labelSmall,color=Color.White.copy(.82f),maxLines=1)
        Spacer(Modifier.weight(1f))
        Text(value,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Black,color=Color.White,maxLines=1)
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    tint: Color,
    deposit: Boolean,
    modifier: Modifier = Modifier,
    /** رنگ متن وقتی چیپ روی کارت بانکی (با رنگ خود حساب) می‌نشیند. */
    onColor: Color? = null,
    /** با زدن چیپ، فهرست پایین صفحه فیلتر می‌شود. */
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(16.dp)
    val fg = onColor ?: skin.onHero
    // روی متن روشن، پس‌زمینه تیره و برعکس؛ تا چیپ روی هر رنگ کارتی خوانا بماند
    val chipBg = if (onColor == null) heroChipBg(skin)
        else if (fg.luminance() > 0.5f) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.72f)
    Row(
        modifier = modifier
            .clip(shape)
            // پس‌زمینه کنتراست‌دار نسبت به کارت: روی تم روشن، روشن؛ روی تم تیره، تیره
            .background(if (selected) tint.copy(alpha = 0.22f) else chipBg)
            // حاشیه نازک هم‌رنگ مقدار؛ در حالت انتخاب‌شده پررنگ‌تر
            .border(if (selected) 2.dp else 1.dp, tint.copy(alpha = if (selected) 0.95f else 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (deposit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(label,style=MaterialTheme.typography.labelSmall,color=fg.copy(alpha=.92f),maxLines=1)
        Spacer(Modifier.weight(1f))
        EmbossedText(
            value,
            style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start),
            fontWeight = FontWeight.Bold,
            color = tint,
            maxLines = 1,
            depth = 0.6f
        )
    }
}



/** سری روزانه درآمد/هزینه برای n روز اخیر (بر اساس روز شمسی/منطقه زمانی تهران). */
internal fun buildDailySeries(
    txs: List<ir.kharjyar.app.data.db.TransactionEntity>,
    days: Int
): Pair<List<Long>, List<Long>> {
    val today = PersianDate.today()
    val income = MutableList(days) { 0L }
    val expense = MutableList(days) { 0L }
    val dayStartList = (0 until days).map { today.plusDays(-(days - 1 - it)) }
    val starts = dayStartList.map { it.startOfDayMillis() }
    val ends = dayStartList.map { it.endOfDayMillisExclusive() }
    for (tx in txs) {
        if (tx.status != TxStatus.CONFIRMED) continue
        if (tx.nature == ir.kharjyar.app.data.db.TxNature.TRANSFER) continue
        for (i in 0 until days) {
            if (tx.occurredAt >= starts[i] && tx.occurredAt < ends[i]) {
                val isIncome = tx.nature == ir.kharjyar.app.data.db.TxNature.INCOME ||
                    (tx.nature == ir.kharjyar.app.data.db.TxNature.UNKNOWN && tx.direction == ir.kharjyar.app.data.db.TxDirection.DEPOSIT)
                if (isIncome) income[i] += tx.amountRial else expense[i] += tx.amountRial
                break
            }
        }
    }
    return income to expense
}

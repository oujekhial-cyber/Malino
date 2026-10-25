package ir.kharjyar.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ModernChoiceDialog
import ir.kharjyar.app.ui.components.ModernChoiceOption
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.PersianDateField
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch
import java.io.File

@Composable fun ChecksScreen(vm:AppViewModel,initialCheckId:Long?=null){val context=LocalContext.current;val checks by vm.checks.collectAsState();val accounts by vm.accounts.collectAsState();val settings by vm.settings.collectAsState();val pending by vm.pendingFeatureCommand.collectAsState();val scope=rememberCoroutineScope();var entryDirection by remember(pending){mutableStateOf<Int?>(when(pending?.action){"check_issued"->CheckDirection.ISSUED;"check_received"->CheckDirection.RECEIVED;else->null})};var showChooser by remember{mutableStateOf(false)};var filter by remember{mutableStateOf<Int?>(null)};var pendingClear by remember{mutableStateOf<CheckEntity?>(null)};var pendingDelete by remember{mutableStateOf<CheckEntity?>(null)};var editingCheck by remember{mutableStateOf<CheckEntity?>(null)};var selectedCheck by remember(checks,initialCheckId){mutableStateOf(checks.firstOrNull{it.id==initialCheckId})};var clearAccountId by remember{mutableStateOf<Long?>(null)};var clearDate by remember{mutableStateOf(PersianDate.today())}
 BackHandler(enabled=entryDirection!=null){entryDirection=null;editingCheck=null}
 if(entryDirection!=null){CheckEntryPage(vm,entryDirection!!,editingCheck,pending?.takeIf{it.action=="check_issued"||it.action=="check_received"},{entryDirection=null;editingCheck=null;vm.clearFeatureCommand()},{entryDirection=null;editingCheck=null;vm.clearFeatureCommand()});return}
 selectedCheck?.let{check->SayadCheckDetail(vm,check,settings.moneyUnit,{selectedCheck=it}){selectedCheck=null};return}
 val shown=checks.filter{filter==null||it.direction==filter}.sortedBy{it.dueAt};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){ModernSummaryHero("مدیریت چک‌ها","چک‌های در انتظار وصول یا پرداخت",MaterialTheme.colorScheme.primary,listOf(SummaryMetric("دریافتی باز",Money.format(checks.filter{it.direction==CheckDirection.RECEIVED&&it.status==CheckStatus.PENDING}.sumOf{it.amountRial},settings.moneyUnit),Color(0xFF1B9A61),Icons.Filled.CallReceived),SummaryMetric("صادرشده باز",Money.format(checks.filter{it.direction==CheckDirection.ISSUED&&it.status==CheckStatus.PENDING}.sumOf{it.amountRial},settings.moneyUnit),Color(0xFFE24B57),Icons.Filled.CallMade)),Icons.Filled.FactCheck);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter==null,{filter=null},{Text("همه")});FilterChip(filter==CheckDirection.RECEIVED,{filter=CheckDirection.RECEIVED},{Text("دریافتی")});FilterChip(filter==CheckDirection.ISSUED,{filter=CheckDirection.ISSUED},{Text("صادرشده")})};if(shown.isEmpty())Text("هنوز چکی ثبت نشده است.",modifier=Modifier.padding(vertical=24.dp));shown.forEach { check ->
   SwipeActionRow(
    onDelete = { pendingDelete = check },
    onEdit = { editingCheck = check; entryDirection = check.direction },
    removeOnDelete = false
   ) {
    ModernCheckListCard(
     check = check,
     moneyUnit = settings.moneyUnit,
     onOpen = { selectedCheck = check },
     onClear = { pendingClear = check; clearAccountId = check.accountId; clearDate = PersianDate.today() },
     onBounce = { scope.launch { vm.repo.db.checkDao().update(check.copy(status = CheckStatus.BOUNCED)) } }
    )
   }
  }}
  FloatingActionButton({showChooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp),containerColor=MaterialTheme.colorScheme.primary){Icon(Icons.Filled.Add,"افزودن چک")}}
 if(showChooser)ModernChoiceDialog("نوع چک","مسیر ثبت چک را انتخاب کنید",listOf(ModernChoiceOption("چک دریافت‌شده","چکی که از شخص دیگری دریافت کرده‌اید",Color(0xFF1B8F52),Icons.Filled.CallReceived){showChooser=false;entryDirection=CheckDirection.RECEIVED},ModernChoiceOption("چک صادرشده","چکی که برای شخص دیگری صادر کرده‌اید",Color(0xFFD33B45),Icons.Filled.CallMade){showChooser=false;entryDirection=CheckDirection.ISSUED}),{showChooser=false})
 pendingDelete?.let{check->AlertDialog(onDismissRequest={pendingDelete=null},title={Text("حذف چک؟")},text={Text("چک مربوط به «${check.counterparty}» حذف شود؟ تراکنش وصول یا پاس‌شدن که قبلاً ثبت شده باشد حذف نخواهد شد.")},confirmButton={TextButton({scope.launch{vm.repo.db.checkDao().delete(check);check.imagePath.takeIf{it.isNotBlank()}?.let{runCatching{File(it).delete()}};pendingDelete=null}}){Text("حذف",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton({pendingDelete=null}){Text("انصراف")}})}
 pendingClear?.let { check ->
  val received=check.direction==CheckDirection.RECEIVED
  AlertDialog(onDismissRequest={pendingClear=null},title={Text(if(received)"ثبت وصول چک" else "ثبت پاس شدن چک")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text(if(received)"مبلغ چک به کدام حساب واریز شده است؟" else "مبلغ چک از کدام حساب برداشت شده است؟");ComboBox(label="حساب مالی *",options=accounts.filter{!it.archived}.map{it.id},selected=clearAccountId,onSelect={clearAccountId=it},labelOf={id->accounts.firstOrNull{it.id==id}?.let{"${it.title} — ${it.bankName}"}?:"حساب"});Text("تاریخ وصول/پاس شدن *");PersianDateField(clearDate,{clearDate=it});Text("با تأیید، تراکنش ${if(received)"واریز" else "برداشت"} این حساب نیز ثبت می‌شود.",style=MaterialTheme.typography.bodySmall)}},confirmButton={TextButton(enabled=clearAccountId!=null,onClick={val accountId=clearAccountId?:return@TextButton;scope.launch{val number=check.sayadId.ifBlank{check.serialNumber};val action=if(received)"وصول چک" else "پاس شدن چک";vm.repo.txDao.insert(TransactionEntity(accountId=accountId,amountRial=check.amountRial,direction=if(received)TxDirection.DEPOSIT else TxDirection.WITHDRAW,nature=TxNature.TRANSFER,description="$action به شماره $number به نام ${check.counterparty}",occurredAt=clearDate.startOfDayMillis(),recordedAt=System.currentTimeMillis(),source=TxSource.MANUAL,status=TxStatus.CONFIRMED,refNumber=number));vm.repo.db.checkDao().update(check.copy(status=CheckStatus.CLEARED,accountId=accountId));vm.repo.db.reminderDao().allOnce().filter{it.category=="چک"&&it.note.contains(number)}.forEach{LifeReminderWorker.cancel(context,it.id);vm.repo.db.reminderDao().update(it.copy(enabled=false))};pendingClear=null}}){Text(if(received)"تأیید وصول و ثبت تراکنش" else "تأیید پاس شدن و ثبت تراکنش")}},dismissButton={TextButton({pendingClear=null}){Text("انصراف")}})
 }
}

@Composable
private fun ModernCheckListCard(
 check:CheckEntity,
 moneyUnit:ir.kharjyar.app.core.money.MoneyUnit,
 onOpen:()->Unit,
 onClear:()->Unit,
 onBounce:()->Unit
){
 val received=check.direction==CheckDirection.RECEIVED
 val accent=if(received)Color(0xFF159B73) else Color(0xFFE0525E)
 val statusText=when(check.status){CheckStatus.CLEARED->if(received)"وصول‌شده" else "پاس‌شده";CheckStatus.BOUNCED->"برگشت‌خورده";CheckStatus.CANCELLED->"لغوشده";else->"در انتظار"}
 val statusColor=when(check.status){CheckStatus.CLEARED->Color(0xFF159B73);CheckStatus.BOUNCED->Color(0xFFE0525E);CheckStatus.CANCELLED->MaterialTheme.colorScheme.onSurfaceVariant;else->Color(0xFFE09B2D)}
 Card(modifier=Modifier.fillMaxWidth().border(1.dp,accent.copy(.3f),RoundedCornerShape(24.dp)).clickable(onClick=onOpen),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color.Transparent),elevation=CardDefaults.cardElevation(3.dp)){
  Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent.copy(.17f),MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface))).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Box(Modifier.size(50.dp).background(accent.copy(.16f),RoundedCornerShape(16.dp)).border(1.dp,accent.copy(.32f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Icon(if(received)Icons.Filled.CallReceived else Icons.Filled.CallMade,null,tint=accent,modifier=Modifier.size(27.dp))}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(if(received)"چک دریافت‌شده" else "چک صادرشده",style=MaterialTheme.typography.labelLarge,color=accent);Text(check.counterparty,style=MaterialTheme.typography.titleMedium);val chequePerson=if(received)check.issuerName else check.receiverName;if(chequePerson.isNotBlank()&&chequePerson!=check.counterparty)Text((if(received)"صادرکننده: " else "دریافت‌کننده: ")+chequePerson,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    Surface(color=statusColor.copy(.13f),shape=RoundedCornerShape(50),border=BorderStroke(1.dp,statusColor.copy(.25f))){Text(statusText,Modifier.padding(horizontal=10.dp,vertical=5.dp),color=statusColor,style=MaterialTheme.typography.labelMedium)}
   }
   Text(Money.format(check.amountRial,moneyUnit),style=MaterialTheme.typography.headlineSmall,color=accent)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    CheckInfoChip(Icons.Filled.EventAvailable,"سررسید",PersianDate.fromMillis(check.dueAt).format(),accent,Modifier.weight(1f))
    CheckInfoChip(Icons.Filled.AccountBalance,"بانک",check.bankName.ifBlank{"ثبت نشده"},accent,Modifier.weight(1f))
   }
   if(check.sayadId.isNotBlank())Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Icon(Icons.Filled.QrCode2,null,tint=accent,modifier=Modifier.size(18.dp));Text("شناسه صیادی: ${Digits.toPersian(check.sayadId)}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   HorizontalDivider(color=accent.copy(.16f))
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("لمس برای نمایش چک صیادی",Modifier.weight(1f),style=MaterialTheme.typography.labelSmall,color=accent);Icon(Icons.Filled.ChevronLeft,"نمایش جزئیات",tint=accent);if(check.status==CheckStatus.PENDING){TextButton(onClear){Text(if(received)"ثبت وصول" else "ثبت پاس شدن")};TextButton(onBounce){Text("برگشت خورد",color=MaterialTheme.colorScheme.error)}}}
  }
 }
}

private fun formatSayadId(value:String):String {
 val normalized=Digits.normalize(value).filter{it.isDigit()}
 if(normalized.isBlank())return "—"
 return Digits.ltr(normalized.chunked(4).joinToString("  "){Digits.toPersian(it)})
}

@Composable private fun CheckInfoChip(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,value:String,accent:Color,modifier:Modifier=Modifier){Surface(modifier=modifier,color=accent.copy(.07f),shape=RoundedCornerShape(14.dp)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){Icon(icon,null,tint=accent,modifier=Modifier.size(19.dp));Column{Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.labelMedium,maxLines=1)}}}}

@Composable
private fun SayadCheckDetail(vm:AppViewModel,check:CheckEntity,moneyUnit:ir.kharjyar.app.core.money.MoneyUnit,onImageSaved:(CheckEntity)->Unit,onBack:()->Unit){
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val activity=context as? Activity
 var cameraUri by remember{mutableStateOf<android.net.Uri?>(null)}
 var showOriginal by remember{mutableStateOf(false)}
 var documentMessage by remember{mutableStateOf<String?>(null)}
 var immersive by remember{mutableStateOf(false)}
 var zoom by remember{mutableStateOf(1f)}
 var pan by remember{mutableStateOf(Offset.Zero)}
 fun saveDocument(uri:android.net.Uri){runCatching{val dir=File(context.filesDir,"checks").apply{mkdirs()};val file=File(dir,"check-${check.id}-${System.currentTimeMillis()}.jpg");context.contentResolver.openInputStream(uri)?.use{input->file.outputStream().use{input.copyTo(it)}}?:error("تصویر خوانده نشد");val updated=check.copy(imagePath=file.absolutePath);scope.launch{vm.repo.db.checkDao().update(updated);onImageSaved(updated);documentMessage="عکس اصلی چک ذخیره شد"}}.onFailure{documentMessage="ذخیره عکس چک انجام نشد"}}
 val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){it?.let(::saveDocument)}
 val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ok->if(ok)cameraUri?.let(::saveDocument)}
 fun openCamera(){val file=File(context.cacheDir,"check-document-${check.id}-${System.currentTimeMillis()}.jpg");val uri=androidx.core.content.FileProvider.getUriForFile(context,"${context.packageName}.files",file);cameraUri=uri;camera.launch(uri)}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted)openCamera()else documentMessage="اجازه دوربین داده نشد"}

 DisposableEffect(activity){
  val previous=activity?.requestedOrientation
  activity?.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
  onDispose{
   activity?.requestedOrientation=previous?:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
   activity?.window?.let{window->WindowCompat.setDecorFitsSystemWindows(window,true);WindowCompat.getInsetsController(window,window.decorView).show(WindowInsetsCompat.Type.systemBars())}
  }
 }
 LaunchedEffect(immersive,activity){activity?.window?.let{window->
  val controller=WindowCompat.getInsetsController(window,window.decorView)
  controller.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
  if(immersive){WindowCompat.setDecorFitsSystemWindows(window,false);controller.hide(WindowInsetsCompat.Type.systemBars())}
  else{controller.show(WindowInsetsCompat.Type.systemBars());WindowCompat.setDecorFitsSystemWindows(window,true)}
 }}
 BackHandler{if(immersive){immersive=false}else onBack()}
 val received=check.direction==CheckDirection.RECEIVED
 val ink=Color(0xFF2A1740)
 val paper=Color(0xFFFFF7FC)
 val personOnCheck=(if(received)check.issuerName else check.receiverName).ifBlank{check.counterparty}
 val edgePadding by animateDpAsState(if(immersive)0.dp else 16.dp,tween(520),label="check-edge")

 Box(Modifier.fillMaxSize().background(Color(0xFF101019)).padding(edgePadding).pointerInput(Unit){detectTapGestures{immersive=!immersive}}){
  AnimatedVisibility(!immersive,Modifier.align(Alignment.TopStart),enter=fadeIn(tween(520))+slideInVertically(tween(520)){ -it/2 },exit=fadeOut(tween(420))+slideOutVertically(tween(420)){ -it/2 }){
   IconButton(onBack,Modifier.background(Color.Black.copy(.42f),CircleShape)){Icon(Icons.Filled.ArrowBack,"بازگشت",tint=Color.White)}
  }
  Column(Modifier.align(Alignment.Center).fillMaxWidth(if(immersive)1f else .96f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
   AnimatedVisibility(!immersive,enter=fadeIn(tween(520)),exit=fadeOut(tween(380))){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){
     Text(if(received)"نمای چک دریافت‌شده" else "نمای چک صادرشده",color=Color.White,style=MaterialTheme.typography.titleMedium)
     Spacer(Modifier.width(18.dp));OutlinedButton({if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera()else permission.launch(Manifest.permission.CAMERA)}){Icon(Icons.Filled.PhotoCamera,null);Spacer(Modifier.width(5.dp));Text("عکس با دوربین")}
     Spacer(Modifier.width(7.dp));OutlinedButton({gallery.launch("image/*")}){Icon(Icons.Filled.PhotoLibrary,null);Spacer(Modifier.width(5.dp));Text("انتخاب از گالری")}
     if(check.imagePath.isNotBlank()){Spacer(Modifier.width(7.dp));Button({showOriginal=true}){Icon(Icons.Filled.Image,null);Spacer(Modifier.width(5.dp));Text("عکس اصلی")}}
    }
   }
   documentMessage?.let{if(!immersive)Text(it,color=Color.White,style=MaterialTheme.typography.labelMedium)}
   Card(
    Modifier.fillMaxWidth().aspectRatio(2.066f)
     .graphicsLayer{scaleX=zoom;scaleY=zoom;translationX=pan.x;translationY=pan.y}
     .pointerInput(Unit){detectTransformGestures{_,panChange,zoomChange,_->val next=(zoom*zoomChange).coerceIn(1f,4f);zoom=next;pan=if(next==1f)Offset.Zero else pan+panChange}}
     .border(if(immersive)0.dp else 2.dp,Color(0xFFB67AB6),RoundedCornerShape(if(immersive)0.dp else 14.dp)),
    shape=RoundedCornerShape(if(immersive)0.dp else 14.dp),colors=CardDefaults.cardColors(containerColor=paper),elevation=CardDefaults.cardElevation(if(immersive)0.dp else 10.dp)
   ){
    BoxWithConstraints(Modifier.fillMaxSize().background(paper)){
     Image(painterResource(ir.kharjyar.app.R.drawable.sayad_cheque_template),"تصویر خام چک صیادی",Modifier.matchParentSize(),contentScale=ContentScale.FillBounds)
     fun x(f:Float)=maxWidth*f
     fun y(f:Float)=maxHeight*f
     // جای‌گذاری اطلاعات دقیقاً مطابق ساختار چک صیادی مرجع، بدون بخش ته‌چک.
     Text(check.bankName.ifBlank{"بانک صادرکننده"},Modifier.offset(x(.43f),y(.105f)).width(maxWidth*.20f),color=ink,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.titleMedium)
     Text(Digits.ltr(Digits.toPersian(check.serialNumber.ifBlank{"—"})),Modifier.offset(x(.16f),y(.055f)).width(maxWidth*.25f),color=ink,fontWeight=FontWeight.Bold,maxLines=1,style=MaterialTheme.typography.labelMedium)
     Text(formatSayadId(check.sayadId),Modifier.offset(x(.16f),y(.215f)).width(maxWidth*.26f),color=ink,fontWeight=FontWeight.Bold,maxLines=1,style=MaterialTheme.typography.titleSmall)
     Text(PersianDate.fromMillis(check.dueAt).format(),Modifier.offset(x(.68f),y(.105f)).width(maxWidth*.18f),color=ink,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.titleSmall)
     Text(check.amountInWords.ifBlank{Money.format(check.amountRial,moneyUnit)},Modifier.offset(x(.18f),y(.34f)).width(maxWidth*.57f),color=ink,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.titleSmall)
     Text(personOnCheck,Modifier.offset(x(.19f),y(.48f)).width(maxWidth*.67f),color=ink,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.titleSmall)
     Text(Money.format(check.amountRial,moneyUnit),Modifier.offset(x(.055f),y(.665f)).width(maxWidth*.37f),color=ink,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.titleMedium)
     Text(Digits.ltr(Digits.toPersian(check.chequeAccountNumber.ifBlank{"—"})),Modifier.offset(x(.17f),y(.815f)).width(maxWidth*.36f),color=ink,fontWeight=FontWeight.Bold,maxLines=1,style=MaterialTheme.typography.labelMedium)
     Text(Digits.ltr(Digits.toPersian(check.chequeIban.ifBlank{"—"})),Modifier.offset(x(.17f),y(.895f)).width(maxWidth*.36f),color=ink,fontWeight=FontWeight.Bold,maxLines=1,style=MaterialTheme.typography.labelMedium)
     if(check.nationalId.isNotBlank())Text("شناسه ملی: ${Digits.ltr(Digits.toPersian(check.nationalId))}",Modifier.offset(x(.57f),y(.84f)).width(maxWidth*.25f),color=ink,fontWeight=FontWeight.Bold,maxLines=1,style=MaterialTheme.typography.labelSmall)
     if(check.issuerName.isNotBlank())Text(check.issuerName,Modifier.offset(x(.69f),y(.68f)).width(maxWidth*.23f),color=ink.copy(.84f),textAlign=TextAlign.Center,maxLines=1,style=MaterialTheme.typography.labelMedium)
    }
   }
  }
  AnimatedVisibility(!immersive,Modifier.align(Alignment.BottomCenter),enter=fadeIn(tween(650)),exit=fadeOut(tween(300))){Text("یک‌بار لمس: نمایش تمام‌صفحه  •  دو انگشت: بزرگ‌نمایی و جابه‌جایی",color=Color.White.copy(.78f),style=MaterialTheme.typography.labelSmall,modifier=Modifier.background(Color.Black.copy(.42f),RoundedCornerShape(50)).padding(horizontal=14.dp,vertical=6.dp))}
 }
 if(showOriginal&&check.imagePath.isNotBlank()){
  val bitmap=remember(check.imagePath){android.graphics.BitmapFactory.decodeFile(check.imagePath)?.asImageBitmap()}
  AlertDialog(onDismissRequest={showOriginal=false},title={Text("عکس اصلی چک")},text={if(bitmap!=null)Image(bitmap,"عکس ثبت‌شده چک",Modifier.fillMaxWidth().aspectRatio(1.75f),contentScale=ContentScale.Fit)else Text("فایل عکس در دسترس نیست.")},confirmButton={TextButton({showOriginal=false}){Text("بستن")}})
 }
}

@Composable private fun CheckPaperLine(label:String,value:String,ink:Color){Column(verticalArrangement=Arrangement.spacedBy(2.dp)){Text(label,color=ink.copy(.62f),style=MaterialTheme.typography.labelSmall);Text(value,color=ink,style=MaterialTheme.typography.titleSmall,maxLines=1);Box(Modifier.fillMaxWidth().height(1.dp).background(ink.copy(.22f)))}}

@Composable private fun CheckEntryPage(vm:AppViewModel,direction:Int,existing:CheckEntity?=null,draft:AppViewModel.PendingFeatureCommand?=null,onDone:()->Unit,onCancel:()->Unit){val scope=rememberCoroutineScope();val context=LocalContext.current;val settings by vm.settings.collectAsState();val received=direction==CheckDirection.RECEIVED;var issuer by remember(existing?.id){mutableStateOf(existing?.issuerName.orEmpty())};var receiver by remember(existing?.id){mutableStateOf(existing?.receiverName.orEmpty())};var nationalId by remember(existing?.id,draft){mutableStateOf(existing?.nationalId?:draft?.nationalId.orEmpty())};var chequeAccount by remember(existing?.id,draft){mutableStateOf(existing?.chequeAccountNumber?:draft?.chequeAccountNumber.orEmpty())};var chequeIban by remember(existing?.id,draft){mutableStateOf(existing?.chequeIban?:draft?.chequeIban.orEmpty())};var amount by remember(existing?.id,draft,settings.moneyUnit){mutableStateOf(existing?.amountRial?.let{if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN&&it%10L==0L)(it/10L).toString()else it.toString()}?:draft?.amountRial?.let{if(settings.moneyUnit==ir.kharjyar.app.core.money.MoneyUnit.TOMAN&&it%10L==0L)(it/10L).toString()else it.toString()}.orEmpty())};var party by remember(existing?.id,draft){mutableStateOf(existing?.counterparty?:draft?.party?.ifBlank{parseCheckParty(draft?.details.orEmpty())}.orEmpty())};var bank by remember(existing?.id,draft){mutableStateOf(existing?.bankName?:draft?.bankName?.ifBlank{parseCheckBank(draft?.details.orEmpty())}.orEmpty())};var sayad by remember(existing?.id,draft){mutableStateOf(existing?.sayadId?:draft?.sayadId?.ifBlank{Regex("(?<!\\d)\\d{16}(?!\\d)").find(Digits.normalize(draft?.details.orEmpty()))?.value.orEmpty()}.orEmpty())};var serial by remember(existing?.id,draft){mutableStateOf(existing?.serialNumber?:draft?.serialNumber.orEmpty())};var due by remember(existing?.id,draft){mutableStateOf(existing?.dueAt?.let(PersianDate::fromMillis)?:parseCheckDraftDate(draft?.dueDate,draft?.details)?:PersianDate.today())};var imagePath by remember(existing?.id){mutableStateOf(existing?.imagePath.orEmpty())};var cameraUri by remember{mutableStateOf<android.net.Uri?>(null)};var cameraError by remember{mutableStateOf(false)};var differentChequePerson by remember(existing?.id){mutableStateOf(existing?.let{if(received)it.issuerName.isNotBlank()&&it.issuerName!=it.counterparty else it.receiverName.isNotBlank()&&it.receiverName!=it.counterparty}?:false)}
 fun analyze(uri:android.net.Uri){val dir=File(context.filesDir,"checks").apply{mkdirs()};val file=File(dir,"${System.currentTimeMillis()}.jpg");context.contentResolver.openInputStream(uri)?.use{input->file.outputStream().use{input.copyTo(it)}};imagePath=file.absolutePath;TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromFilePath(context,uri)).addOnSuccessListener{result->val p=ir.kharjyar.app.core.check.SayadCheckParser.parse(result.text);if(p.sayadId.isNotBlank())sayad=p.sayadId;if(p.serialNumber.isNotBlank())serial=p.serialNumber;p.amountRial?.let{amount=it.toString()};p.dueDate?.let{due=it};if(p.bankName.isNotBlank())bank=p.bankName;if(p.issuerName.isNotBlank()&&received){issuer=p.issuerName;party=p.issuerName};if(p.receiverName.isNotBlank()&&!received){receiver=p.receiverName;party=p.receiverName};if(p.nationalId.isNotBlank())nationalId=p.nationalId;if(p.accountNumber.isNotBlank())chequeAccount=p.accountNumber;if(p.iban.isNotBlank())chequeIban=p.iban}}
 val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){it?.let(::analyze)};val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ok->if(ok)cameraUri?.let(::analyze)};fun openCamera(){val f=File(context.cacheDir,"check-camera-${System.currentTimeMillis()}.jpg");val uri=androidx.core.content.FileProvider.getUriForFile(context,"${context.packageName}.files",f);cameraUri=uri;camera.launch(uri)};val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->cameraError=!granted;if(granted)openCamera()}
 val accent=if(received)Color(0xFF1B8F52) else Color(0xFFD33B45);Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};ModernSummaryHero(if(existing!=null)"ویرایش چک" else if(received)"ثبت چک دریافت‌شده" else "ثبت چک صادرشده","اطلاعات صیادی و سررسید",accent,listOf(SummaryMetric("مبلغ",Money.inputToRial(amount,settings.moneyUnit)?.let{Money.format(it,settings.moneyUnit)}?:"—",accent,if(received)Icons.Filled.CallReceived else Icons.Filled.CallMade),SummaryMetric("سررسید",due.format(),MaterialTheme.colorScheme.primary,Icons.Filled.FactCheck)),Icons.Filled.FactCheck);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera()else permission.launch(Manifest.permission.CAMERA)},Modifier.weight(1f)){Text("اسکن با دوربین")};OutlinedButton({gallery.launch("image/*")},Modifier.weight(1f)){Text("انتخاب از گالری")}};if(cameraError)Text("اجازه دوربین داده نشده است.",color=MaterialTheme.colorScheme.error);if(imagePath.isNotBlank())Text("تصویر خوانده شد؛ اطلاعات را بررسی کنید.",color=accent);OutlinedTextField(party,{party=it},label={Text("طرف حساب اصلی *")},modifier=Modifier.fillMaxWidth());Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.08f))){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(if(received)"صادرکننده چک شخص دیگری است" else "دریافت‌کننده چک شخص دیگری است",style=MaterialTheme.typography.titleSmall);Text(if(received)"اگر چک را فردی غیر از طرف حساب صادر کرده، فعال کنید." else "اگر چک به نام فردی غیر از طرف حساب است، فعال کنید.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(differentChequePerson,{differentChequePerson=it;if(it){if(received&&issuer.isBlank())issuer=party;if(!received&&receiver.isBlank())receiver=party}})};if(differentChequePerson){if(received)OutlinedTextField(issuer,{issuer=it},label={Text("نام صادرکننده چک *")},modifier=Modifier.fillMaxWidth()) else OutlinedTextField(receiver,{receiver=it},label={Text("نام دریافت‌کننده چک *")},modifier=Modifier.fillMaxWidth())}else Text(if(received)"صادرکننده چک همان طرف حساب در نظر گرفته می‌شود." else "دریافت‌کننده چک همان طرف حساب در نظر گرفته می‌شود.",style=MaterialTheme.typography.labelSmall,color=accent)}};OutlinedTextField(nationalId,{nationalId=Digits.normalize(it).filter(Char::isDigit)},label={Text("کد/شناسه ملی")},modifier=Modifier.fillMaxWidth());AmountTextField(amount,{amount=it},"مبلغ چک به عدد *",Modifier.fillMaxWidth(),unit=settings.moneyUnit);OutlinedTextField(bank,{bank=it},label={Text("بانک و شعبه")},modifier=Modifier.fillMaxWidth());OutlinedTextField(sayad,{sayad=Digits.normalize(it).filter(Char::isDigit)},label={Text("شناسه ۱۶ رقمی صیادی *")},modifier=Modifier.fillMaxWidth());OutlinedTextField(serial,{serial=Digits.normalize(it).filter(Char::isDigit)},label={Text("شماره سریال/سری چک")},modifier=Modifier.fillMaxWidth());OutlinedTextField(chequeAccount,{chequeAccount=Digits.normalize(it)},label={Text("شماره حساب چک")},modifier=Modifier.fillMaxWidth());OutlinedTextField(chequeIban,{chequeIban=it.uppercase()},label={Text("شماره شبا")},modifier=Modifier.fillMaxWidth());Text("تاریخ سررسید *");PersianDateField(due,{due=it});Button({scope.launch{val record=CheckEntity(id=existing?.id?:0,direction=direction,amountRial=Money.inputToRial(amount,settings.moneyUnit)?:0,counterparty=party.trim(),accountId=existing?.accountId,status=existing?.status?:CheckStatus.PENDING,note=existing?.note.orEmpty(),bankName=bank.trim(),sayadId=sayad,serialNumber=serial,issuedAt=existing?.issuedAt?:System.currentTimeMillis(),dueAt=due.startOfDayMillis(),reminderAt=null,imagePath=imagePath,issuerName=if(received){if(differentChequePerson)issuer.trim() else party.trim()}else "",receiverName=if(!received){if(differentChequePerson)receiver.trim() else party.trim()}else "",nationalId=nationalId,chequeAccountNumber=chequeAccount,chequeIban=chequeIban);if(existing==null)vm.repo.db.checkDao().insert(record)else vm.repo.db.checkDao().update(record);val checkNumber=sayad.ifBlank{serial};if(existing!=null){val oldNumber=existing.sayadId.ifBlank{existing.serialNumber};vm.repo.db.reminderDao().allOnce().filter{it.category=="چک"&&it.note.contains(oldNumber)}.forEach{LifeReminderWorker.cancel(context,it.id);vm.repo.db.reminderDao().update(it.copy(enabled=false))}};val dueReminderAt=due.startOfDayMillis()+9*60*60*1000L;val beforeReminderAt=due.plusDays(-1).startOfDayMillis()+9*60*60*1000L;val reminderNote="چک ${if(received)"دریافتی" else "صادرشده"} شماره $checkNumber — طرف حساب: $party";val beforeId=vm.repo.db.reminderDao().insert(ReminderEntity(title="یادآوری چک؛ یک روز مانده به سررسید",category="چک",note=reminderNote,nextAt=beforeReminderAt));val dueId=vm.repo.db.reminderDao().insert(ReminderEntity(title="موعد چک",category="چک",note=reminderNote,nextAt=dueReminderAt));LifeReminderWorker.schedule(context,beforeId,beforeReminderAt);LifeReminderWorker.schedule(context,dueId,dueReminderAt);showSavedMessage(context,"چک");onDone()}},enabled=party.isNotBlank()&&(!differentChequePerson||(if(received)issuer else receiver).isNotBlank())&&(Money.inputToRial(amount,settings.moneyUnit)?:0)>0&&sayad.length==16,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text(if(existing!=null)"ذخیره تغییرات چک" else if(received)"ثبت چک دریافت‌شده و یادآور" else "ثبت چک صادرشده و یادآور")}}
}

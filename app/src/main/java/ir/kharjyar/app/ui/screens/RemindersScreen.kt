package ir.kharjyar.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ir.kharjyar.app.ui.components.ThemedFloatingActionButton
import ir.kharjyar.app.ui.components.ModernChoiceDialog
import ir.kharjyar.app.ui.components.ModernChoiceOption
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.components.ComboBox
import ir.kharjyar.app.ui.components.DateTimeField
import ir.kharjyar.app.ui.components.ModernSummaryHero
import ir.kharjyar.app.ui.components.SummaryMetric
import ir.kharjyar.app.work.LifeReminderWorker
import kotlinx.coroutines.launch

@Composable fun RemindersScreen(vm:AppViewModel){val reminders by vm.repo.db.reminderDao().observeAll().collectAsState(initial=emptyList());val context=LocalContext.current;val scope=rememberCoroutineScope();var entryRepeat by remember{mutableStateOf<Int?>(null)};var chooser by remember{mutableStateOf(false)};var filter by remember{mutableStateOf<Int?>(null)};var editingReminder by remember{mutableStateOf<ReminderEntity?>(null)}
 BackHandler(enabled=entryRepeat!=null){entryRepeat=null}
 if(entryRepeat!=null){ReminderEntryPage(vm,entryRepeat!!,{entryRepeat=null},{entryRepeat=null});return}
 val shown=reminders.filter{filter==null||(filter==1&&it.enabled)||(filter==0&&!it.enabled)};val active=reminders.count{it.enabled};Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,top=12.dp,bottom=92.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){ModernSummaryHero("یادآورها","برنامه‌ریزی کارها و موعدهای مهم",Color(0xFF6A5AE0),listOf(SummaryMetric("فعال",Digits.toPersian(active.toString()),Color(0xFF1B9A61),Icons.Filled.NotificationsActive),SummaryMetric("انجام‌شده",Digits.toPersian((reminders.size-active).toString()),Color(0xFF6A5AE0),Icons.Filled.TaskAlt),SummaryMetric("موعد بعدی",reminders.filter{it.enabled}.minByOrNull{it.nextAt}?.let{PersianDate.fromMillis(it.nextAt).format()}?:"—",Color(0xFFE59A24),Icons.Filled.EventAvailable)),Icons.Filled.NotificationsActive);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter==null,{filter=null},{Text("همه")});FilterChip(filter==1,{filter=1},{Text("فعال")});FilterChip(filter==0,{filter=0},{Text("انجام‌شده")})};if(shown.isEmpty())Card(Modifier.fillMaxWidth()){Text("در این بخش هنوز یادآوری وجود ندارد.",Modifier.padding(24.dp))};shown.forEach{r->val accent=if(r.enabled)Color(0xFF20A565) else Color(0xFFE14B55);Card(colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f)),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().border(1.3.dp,accent,RoundedCornerShape(16.dp))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(r.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(if(r.enabled)"فعال" else "انجام‌شده",color=accent,fontWeight=FontWeight.Bold)};Text(r.category,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(PersianDate.formatDateTime(r.nextAt),style=MaterialTheme.typography.titleSmall);Text(repeatLabel(r.repeatType,r.repeatInterval),color=accent);if(r.note.isNotBlank())Text(r.note);Row{TextButton({editingReminder=r}){Text("تغییر زمان")};TextButton({scope.launch{val changed=r.copy(enabled=!r.enabled);vm.repo.db.reminderDao().update(changed);if(changed.enabled)LifeReminderWorker.schedule(context,r.id,r.nextAt)else LifeReminderWorker.cancel(context,r.id)}}){Text(if(r.enabled)"علامت‌گذاری به‌عنوان انجام‌شده" else "فعال‌سازی دوباره")};TextButton({scope.launch{LifeReminderWorker.cancel(context,r.id);vm.repo.db.reminderDao().delete(r.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}}}}}
 }
 ThemedFloatingActionButton({chooser=true},Modifier.align(Alignment.BottomEnd).padding(20.dp)){Icon(Icons.Filled.Add,"افزودن یادآور")}
 if(chooser)ModernChoiceDialog("نوع یادآور","نحوه تکرار یادآوری را مشخص کنید",listOf(ModernChoiceOption("یادآور یک‌باره","فقط یک بار در تاریخ و ساعت انتخابی",Color(0xFF1B8F52),Icons.Filled.EventAvailable){chooser=false;entryRepeat=ReminderRepeat.ONCE},ModernChoiceOption("یادآور تکرارشونده","برای کارها و پرداخت‌های دوره‌ای",Color(0xFF6A5AE0),Icons.Filled.Repeat){chooser=false;entryRepeat=ReminderRepeat.MONTHLY}),{chooser=false})
}
 editingReminder?.let{reminder->ReminderTimeEditDialog(reminder,onDismiss={editingReminder=null},onSave={newAt->scope.launch{val changed=reminder.copy(nextAt=newAt,enabled=true,lastNotifiedAt=null);vm.repo.db.reminderDao().update(changed);LifeReminderWorker.cancel(context,reminder.id);LifeReminderWorker.schedule(context,reminder.id,newAt);editingReminder=null}})}
}

@Composable private fun ReminderTimeEditDialog(reminder:ReminderEntity,onDismiss:()->Unit,onSave:(Long)->Unit){
 val calendar=remember(reminder.id,reminder.nextAt){java.util.Calendar.getInstance().apply{timeInMillis=reminder.nextAt}}
 var date by remember(reminder.id,reminder.nextAt){mutableStateOf(PersianDate.fromMillis(reminder.nextAt))}
 var hour by remember(reminder.id,reminder.nextAt){mutableIntStateOf(calendar.get(java.util.Calendar.HOUR_OF_DAY))}
 var minute by remember(reminder.id,reminder.nextAt){mutableIntStateOf(calendar.get(java.util.Calendar.MINUTE))}
 AlertDialog(onDismissRequest=onDismiss,title={Text("تغییر زمان یادآور")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(reminder.title);DateTimeField(date,hour,minute,{date=it},{h,m->hour=h;minute=m})}},confirmButton={TextButton({onSave(date.startOfDayMillis()+(hour*60L+minute)*60_000L)}){Text("ذخیره زمان")}},dismissButton={TextButton(onDismiss){Text("انصراف")}})
}

@Composable private fun ReminderSummary(label:String,count:Int,color:Color,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=color.copy(.10f))){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(Digits.toPersian(count.toString()),style=MaterialTheme.typography.headlineSmall,color=color,fontWeight=FontWeight.Bold);Text(label)}}}
private fun repeatLabel(type:Int,interval:Int)=when(type){ReminderRepeat.ONCE->"بدون تکرار";ReminderRepeat.DAILY->"تکرار روزانه";ReminderRepeat.WEEKLY->"تکرار هفتگی";ReminderRepeat.MONTHLY->"تکرار ماهانه";ReminderRepeat.YEARLY->"تکرار سالانه";else->"هر ${Digits.toPersian(interval.toString())} روز یک‌بار"}

@Composable private fun ReminderEntryPage(vm:AppViewModel,initialRepeat:Int,onDone:()->Unit,onCancel:()->Unit){val context=LocalContext.current;val scope=rememberCoroutineScope();val categories=listOf("تعویض روغن خودرو","قبض برق","قبض آب","قبض گاز","قبض تلفن و اینترنت","بیمه","دارو و سلامت","سرویس و نگهداری","سایر");val repeats=listOf(ReminderRepeat.ONCE,ReminderRepeat.DAILY,ReminderRepeat.WEEKLY,ReminderRepeat.MONTHLY,ReminderRepeat.YEARLY,ReminderRepeat.CUSTOM_DAYS);var category by remember{mutableStateOf(categories.first())};var title by remember{mutableStateOf(categories.first())};var note by remember{mutableStateOf("")};var date by remember{mutableStateOf(PersianDate.today())};var hour by remember{mutableIntStateOf(9)};var minute by remember{mutableIntStateOf(0)};var repeat by remember{mutableIntStateOf(initialRepeat)};var interval by remember{mutableStateOf("1")};val recurring=initialRepeat!=ReminderRepeat.ONCE;val accent=if(recurring)Color(0xFFD33B45) else Color(0xFF1B8F52)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onCancel){Icon(Icons.Filled.ArrowBack,null);Text("بازگشت به فهرست")};ModernSummaryHero(if(recurring)"ثبت یادآور تکرارشونده" else "ثبت یادآور یک‌باره","برای قبض‌ها، سرویس خودرو، بیمه و کارهای روزمره",accent,listOf(SummaryMetric("موعد",date.format(),accent,Icons.Filled.EventAvailable),SummaryMetric("تکرار",if(recurring)repeatLabel(repeat,interval.toIntOrNull()?:1) else "یک‌باره",Color(0xFF6A5AE0),Icons.Filled.Repeat)),Icons.Filled.NotificationsActive);ComboBox("نوع یادآور",categories,category,{category=it;if(title.isBlank()||title in categories)title=it},labelOf={it});OutlinedTextField(title,{title=it},label={Text("عنوان")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("توضیحات (اختیاری)")},modifier=Modifier.fillMaxWidth());DateTimeField(date,hour,minute,{date=it},{h,m->hour=h;minute=m});if(recurring){ComboBox("تکرار",repeats.filter{it!=ReminderRepeat.ONCE},repeat,{repeat=it},labelOf={repeatLabel(it,interval.toIntOrNull()?:1)});if(repeat==ReminderRepeat.CUSTOM_DAYS)OutlinedTextField(interval,{interval=Digits.normalize(it).filter(Char::isDigit)},label={Text("فاصله تکرار (روز)")},modifier=Modifier.fillMaxWidth())};Button({scope.launch{val at=date.startOfDayMillis()+(hour*60L+minute)*60_000L;val id=vm.repo.db.reminderDao().insert(ReminderEntity(title=title.trim(),category=category,note=note.trim(),nextAt=at,repeatType=if(recurring)repeat else ReminderRepeat.ONCE,repeatInterval=interval.toIntOrNull()?.coerceAtLeast(1)?:1));LifeReminderWorker.schedule(context,id,at);showSavedMessage(context,"یادآور");onDone()}},enabled=title.isNotBlank(),modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.5.dp,accent)){Text("ثبت و فعال‌سازی یادآور")}}
}

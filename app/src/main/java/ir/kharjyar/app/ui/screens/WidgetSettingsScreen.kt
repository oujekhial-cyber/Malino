package ir.kharjyar.app.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.data.prefs.*
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.*
import ir.kharjyar.app.widget.KharjYarWidgetReceiver
import ir.kharjyar.app.widget.WidgetUpdater
import kotlinx.coroutines.launch

private enum class WidgetElement(val label:String) { TITLE("نام برنامه"), CLOCK("ساعت"), DATES("تاریخ‌ها"), VALUES("مبالغ"), LABELS("عنوان مبالغ") }
private data class WidgetPosition(val h:WidgetAlign,val v:WidgetVAlign,val label:String)
private val positions=listOf(
 WidgetPosition(WidgetAlign.START,WidgetVAlign.TOP,"بالا سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.TOP,"بالا وسط"),WidgetPosition(WidgetAlign.END,WidgetVAlign.TOP,"بالا سمت چپ"),
 WidgetPosition(WidgetAlign.START,WidgetVAlign.CENTER,"وسط سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.CENTER,"مرکز"),WidgetPosition(WidgetAlign.END,WidgetVAlign.CENTER,"وسط سمت چپ"),
 WidgetPosition(WidgetAlign.START,WidgetVAlign.BOTTOM,"پایین سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.BOTTOM,"پایین وسط"),WidgetPosition(WidgetAlign.END,WidgetVAlign.BOTTOM,"پایین سمت چپ"))

@Composable fun WidgetSettingsScreen(viewModel:AppViewModel){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val settings by viewModel.settings.collectAsState();var city by remember{mutableStateOf(ir.kharjyar.app.weather.WeatherService.city(context))};var element by remember{mutableStateOf(WidgetElement.TITLE)};var saving by remember{mutableStateOf(false)};var saveMessage by remember{mutableStateOf<String?>(null)}
 fun apply(block:suspend()->Unit){scope.launch{block();WidgetUpdater.requestUpdate(context)}}
 fun positionOf():WidgetPosition=when(element){WidgetElement.TITLE->positions.first{it.h==settings.widgetTitleAlign&&it.v==settings.widgetTitleVAlign};WidgetElement.VALUES,WidgetElement.LABELS->positions.first{it.h==settings.widgetValuesAlign&&it.v==settings.widgetValuesVAlign};else->positions.first{it.h==settings.widgetClockAlign&&it.v==settings.widgetClockVAlign}}
 fun size():Int=when(element){WidgetElement.TITLE->settings.widgetTitleSize;WidgetElement.CLOCK->settings.widgetClockSize;WidgetElement.DATES->settings.widgetDateSize;WidgetElement.VALUES->settings.widgetValueSize;WidgetElement.LABELS->settings.widgetLabelSize}
 fun setSize(v:Int)=apply{when(element){WidgetElement.TITLE->viewModel.settingsRepo.setWidgetTitleSize(v);WidgetElement.CLOCK->viewModel.settingsRepo.setWidgetClockSize(v);WidgetElement.DATES->viewModel.settingsRepo.setWidgetDateSize(v);WidgetElement.VALUES->viewModel.settingsRepo.setWidgetValueSize(v);WidgetElement.LABELS->viewModel.settingsRepo.setWidgetLabelSize(v)}}
 val lines=listOf(WidgetPreviewLine("واریز مهر","۵٬۸۷۰٬۰۰۰",true),WidgetPreviewLine("برداشت مهر","۳٬۲۵۰٬۰۰۰",false))
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  WidgetCard("چیدمان لمسی ویجت"){
   Text("هر کپسول را نگه دارید و به جای دلخواه بکشید؛ پس از رهاکردن به نزدیک‌ترین جایگاه سازگار متصل می‌شود. خط‌های همراه هر بخش نیز با همان بخش جابه‌جا می‌شوند.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   WidgetPreview(settings.widgetLayout,settings.widgetOpacity,settings.widgetShowNumbers,lines,showTitle=settings.widgetShowTitle,showClock=settings.widgetShowClock,showDates=settings.widgetShowDates,clockSize=settings.widgetClockSize,valueSize=settings.widgetValueSize,labelSize=settings.widgetLabelSize,editable=true,titleAlign=settings.widgetTitleAlign,titleVAlign=settings.widgetTitleVAlign,clockAlign=settings.widgetClockAlign,clockVAlign=settings.widgetClockVAlign,valuesAlign=settings.widgetValuesAlign,valuesVAlign=settings.widgetValuesVAlign,onTitlePlaced={h,v->apply{viewModel.settingsRepo.setWidgetTitleAlign(h);viewModel.settingsRepo.setWidgetTitleVAlign(v)}},onValuesPlaced={h,v->apply{viewModel.settingsRepo.setWidgetValuesAlign(h);viewModel.settingsRepo.setWidgetValuesVAlign(v)}},onClockPlaced={h,v->apply{viewModel.settingsRepo.setWidgetClockAlign(h);viewModel.settingsRepo.setWidgetClockVAlign(v)}})
  }
  WidgetCard("ظاهر ویجت"){
   ComboBox("تم ویجت",Palette.entries.toList(),settings.widgetPalette,{apply{viewModel.settingsRepo.setWidgetPalette(it)}},labelOf={ir.kharjyar.app.ui.theme.skinOf(it).title})
   ComboBox("قالب",WidgetLayout.entries.toList(),settings.widgetLayout,{apply{viewModel.settingsRepo.setWidgetLayout(it)}},labelOf={widgetLayoutLabel(it)})
   ComboBox("محتوا",WidgetContent.entries.toList(),settings.widgetContent,{apply{viewModel.settingsRepo.setWidgetContent(it)}},labelOf={widgetContentLabel(it)})
   LabeledSlider("شفافیت",settings.widgetOpacity,0..100,{apply{viewModel.settingsRepo.setWidgetOpacity(it)}},"٪")
  }
  WidgetCard("تنظیم جزء انتخابی"){
   ComboBox("جزء ویجت",WidgetElement.entries.toList(),element,{element=it},labelOf={it.label})
   ComboBox("جای قرارگیری",positions,positionOf(),{p->apply{when(element){WidgetElement.TITLE->{viewModel.settingsRepo.setWidgetTitleAlign(p.h);viewModel.settingsRepo.setWidgetTitleVAlign(p.v)};WidgetElement.VALUES,WidgetElement.LABELS->{viewModel.settingsRepo.setWidgetValuesAlign(p.h);viewModel.settingsRepo.setWidgetValuesVAlign(p.v)};else->{viewModel.settingsRepo.setWidgetClockAlign(p.h);viewModel.settingsRepo.setWidgetClockVAlign(p.v)}}}},labelOf={it.label})
   LabeledSlider("اندازه ${element.label}",size(),8..72,{setSize(it)})
   when(element){
    WidgetElement.TITLE->ToggleRow("نمایش نام برنامه","",settings.widgetShowTitle){apply{viewModel.settingsRepo.setWidgetShowTitle(it)}}
    WidgetElement.CLOCK->ToggleRow("نمایش ساعت","",settings.widgetShowClock){apply{viewModel.settingsRepo.setWidgetShowClock(it)}}
    WidgetElement.DATES->ToggleRow("نمایش تاریخ‌ها","",settings.widgetShowDates){apply{viewModel.settingsRepo.setWidgetShowDates(it)}}
    else->ToggleRow("نمایش اعداد","",settings.widgetShowNumbers){apply{viewModel.settingsRepo.setWidgetShowNumbers(it)}}
   }
  }
  WidgetCard("هواشناسی") {OutlinedTextField(city,{city=it;saveMessage=null},label={Text("شهر")},modifier=Modifier.fillMaxWidth());Button({if(!saving)scope.launch{saving=true;saveMessage=null;ir.kharjyar.app.weather.WeatherService.setCity(context,city);val weather=ir.kharjyar.app.weather.WeatherService.refresh(context,force=true);val updated=WidgetUpdater.updateNow(context);saveMessage=when{weather==null->"شهر یا اطلاعات هواشناسی دریافت نشد؛ اتصال اینترنت و نام شهر را بررسی کنید.";!updated->"هواشناسی دریافت شد، اما بروزرسانی ویجت انجام نشد.";else->"تنظیمات و هواشناسی فوراً روی ویجت بروزرسانی شد."};saving=false}},Modifier.fillMaxWidth(),enabled=!saving&&city.isNotBlank()){if(saving)CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Text("ذخیره و بروزرسانی فوری")};saveMessage?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=if(it.startsWith("تنظیمات"))MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}}
  OutlinedButton({apply{viewModel.settingsRepo.resetWidget()}},Modifier.fillMaxWidth()){Text("بازنشانی تنظیمات ویجت به حالت اولیه")}
  OutlinedButton({val m=AppWidgetManager.getInstance(context);if(m.isRequestPinAppWidgetSupported)m.requestPinAppWidget(ComponentName(context,KharjYarWidgetReceiver::class.java),null,null)},Modifier.fillMaxWidth()){Text("افزودن ویجت به صفحه اصلی")}
 }
}

@Composable private fun WidgetCard(title:String,content:@Composable ColumnScope.()->Unit){SkinCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(title,style=MaterialTheme.typography.titleMedium);content()}}}
@Composable private fun ToggleRow(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title);if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall)};Switch(checked,onChange)}}
fun widgetLayoutLabel(l:WidgetLayout)=when(l){WidgetLayout.ROYAL->"لوکس";WidgetLayout.MINIMAL->"مینیمال";WidgetLayout.PANELS->"پنل‌ها";WidgetLayout.STACKED->"ستونی";WidgetLayout.SPLIT->"دوبخشی";WidgetLayout.GLASS->"شیشه‌ای"}
fun widgetContentLabel(c:WidgetContent)=when(c){WidgetContent.SUMMARY->"خلاصه ماه";WidgetContent.TODAY_EXPENSE->"برداشت امروز";WidgetContent.MONTH_EXPENSE->"برداشت ماه";WidgetContent.RECENT->"تراکنش‌های اخیر"}

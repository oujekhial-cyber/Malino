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

private enum class WidgetElement(val label:String) { TITLE("نام برنامه"), WEATHER("هواشناسی"), CLOCK("ساعت"), DATES("تاریخ‌ها"), VALUES("مبالغ"), LABELS("عنوان مبالغ") }
private data class WidgetPosition(val h:WidgetAlign,val v:WidgetVAlign,val label:String)
private val positions=listOf(
 WidgetPosition(WidgetAlign.START,WidgetVAlign.TOP,"بالا سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.TOP,"بالا وسط"),WidgetPosition(WidgetAlign.END,WidgetVAlign.TOP,"بالا سمت چپ"),
 WidgetPosition(WidgetAlign.START,WidgetVAlign.CENTER,"وسط سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.CENTER,"مرکز"),WidgetPosition(WidgetAlign.END,WidgetVAlign.CENTER,"وسط سمت چپ"),
 WidgetPosition(WidgetAlign.START,WidgetVAlign.BOTTOM,"پایین سمت راست"),WidgetPosition(WidgetAlign.CENTER,WidgetVAlign.BOTTOM,"پایین وسط"),WidgetPosition(WidgetAlign.END,WidgetVAlign.BOTTOM,"پایین سمت چپ"))

@Composable fun WidgetSettingsScreen(viewModel:AppViewModel){
 val context=LocalContext.current;val scope=rememberCoroutineScope();val settings by viewModel.settings.collectAsState();var city by remember{mutableStateOf(ir.kharjyar.app.weather.WeatherService.city(context))};var element by remember{mutableStateOf(WidgetElement.TITLE)}
 fun apply(block:suspend()->Unit){scope.launch{block();WidgetUpdater.requestUpdate(context)}}
 fun positionOf():WidgetPosition=when(element){WidgetElement.TITLE,WidgetElement.VALUES,WidgetElement.LABELS->positions.first{it.h==settings.widgetTitleAlign&&it.v==settings.widgetTitleVAlign};WidgetElement.WEATHER->positions.first{it.h==settings.widgetWeatherAlign&&it.v==settings.widgetWeatherVAlign};else->positions.first{it.h==settings.widgetClockAlign&&it.v==settings.widgetClockVAlign}}
 fun size():Int=when(element){WidgetElement.TITLE->settings.widgetTitleSize;WidgetElement.WEATHER->settings.widgetWeatherSize;WidgetElement.CLOCK->settings.widgetClockSize;WidgetElement.DATES->settings.widgetDateSize;WidgetElement.VALUES->settings.widgetValueSize;WidgetElement.LABELS->settings.widgetLabelSize}
 fun setSize(v:Int)=apply{when(element){WidgetElement.TITLE->viewModel.settingsRepo.setWidgetTitleSize(v);WidgetElement.WEATHER->viewModel.settingsRepo.setWidgetWeatherSize(v);WidgetElement.CLOCK->viewModel.settingsRepo.setWidgetClockSize(v);WidgetElement.DATES->viewModel.settingsRepo.setWidgetDateSize(v);WidgetElement.VALUES->viewModel.settingsRepo.setWidgetValueSize(v);WidgetElement.LABELS->viewModel.settingsRepo.setWidgetLabelSize(v)}}
 val lines=listOf(WidgetPreviewLine("واریز مهر","۵٬۸۷۰٬۰۰۰",true),WidgetPreviewLine("برداشت مهر","۳٬۲۵۰٬۰۰۰",false))
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  WidgetCard("پیش‌نمایش"){WidgetPreview(settings.widgetLayout,settings.widgetOpacity,settings.widgetShowNumbers,lines,showTitle=settings.widgetShowTitle,showClock=settings.widgetShowClock,showDates=settings.widgetShowDates,clockSize=settings.widgetClockSize,valueSize=settings.widgetValueSize,labelSize=settings.widgetLabelSize)}
  WidgetCard("ظاهر ویجت"){
   ComboBox("تم ویجت",Palette.entries.toList(),settings.widgetPalette,{apply{viewModel.settingsRepo.setWidgetPalette(it)}},labelOf={ir.kharjyar.app.ui.theme.skinOf(it).title})
   ComboBox("قالب",WidgetLayout.entries.toList(),settings.widgetLayout,{apply{viewModel.settingsRepo.setWidgetLayout(it)}},labelOf={widgetLayoutLabel(it)})
   ComboBox("محتوا",WidgetContent.entries.toList(),settings.widgetContent,{apply{viewModel.settingsRepo.setWidgetContent(it)}},labelOf={widgetContentLabel(it)})
   LabeledSlider("شفافیت",settings.widgetOpacity,0..100,"٪"){apply{viewModel.settingsRepo.setWidgetOpacity(it)}}
  }
  WidgetCard("تنظیم جزء انتخابی"){
   ComboBox("جزء ویجت",WidgetElement.entries.toList(),element,{element=it},labelOf={it.label})
   ComboBox("جای قرارگیری",positions,positionOf(),{p->apply{when(element){WidgetElement.WEATHER->{viewModel.settingsRepo.setWidgetWeatherAlign(p.h);viewModel.settingsRepo.setWidgetWeatherVAlign(p.v)};WidgetElement.TITLE,WidgetElement.VALUES,WidgetElement.LABELS->{viewModel.settingsRepo.setWidgetTitleAlign(p.h);viewModel.settingsRepo.setWidgetTitleVAlign(p.v)};else->{viewModel.settingsRepo.setWidgetClockAlign(p.h);viewModel.settingsRepo.setWidgetClockVAlign(p.v)}}}},labelOf={it.label})
   LabeledSlider("اندازه ${element.label}",size(),8..72){setSize(it)}
   when(element){
    WidgetElement.TITLE->ToggleRow("نمایش نام برنامه","",settings.widgetShowTitle){apply{viewModel.settingsRepo.setWidgetShowTitle(it)}}
    WidgetElement.WEATHER->ToggleRow("نمایش هواشناسی","مستقل از تاریخ و ساعت",settings.widgetShowWeather){apply{viewModel.settingsRepo.setWidgetShowWeather(it)}}
    WidgetElement.CLOCK->ToggleRow("نمایش ساعت","",settings.widgetShowClock){apply{viewModel.settingsRepo.setWidgetShowClock(it)}}
    WidgetElement.DATES->ToggleRow("نمایش تاریخ‌ها","",settings.widgetShowDates){apply{viewModel.settingsRepo.setWidgetShowDates(it)}}
    else->ToggleRow("نمایش اعداد","",settings.widgetShowNumbers){apply{viewModel.settingsRepo.setWidgetShowNumbers(it)}}
   }
  }
  WidgetCard("هواشناسی") {OutlinedTextField(city,{city=it},label={Text("شهر")},modifier=Modifier.fillMaxWidth());Button({ir.kharjyar.app.weather.WeatherService.setCity(context,city);apply{ir.kharjyar.app.weather.WeatherService.refresh(context)}},Modifier.fillMaxWidth()){Text("ذخیره و بروزرسانی")}}
  OutlinedButton({apply{viewModel.settingsRepo.resetWidget()}},Modifier.fillMaxWidth()){Text("بازنشانی تنظیمات ویجت به حالت اولیه")}
  OutlinedButton({val m=AppWidgetManager.getInstance(context);if(m.isRequestPinAppWidgetSupported)m.requestPinAppWidget(ComponentName(context,KharjYarWidgetReceiver::class.java),null,null)},Modifier.fillMaxWidth()){Text("افزودن ویجت به صفحه اصلی")}
 }
}

@Composable private fun WidgetCard(title:String,content:@Composable ColumnScope.()->Unit){SkinCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(title,style=MaterialTheme.typography.titleMedium);content()}}}
@Composable private fun ToggleRow(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title);if(subtitle.isNotBlank())Text(subtitle,style=MaterialTheme.typography.bodySmall)};Switch(checked,onChange)}}
fun widgetLayoutLabel(l:WidgetLayout)=when(l){WidgetLayout.ROYAL->"لوکس";WidgetLayout.MINIMAL->"مینیمال";WidgetLayout.PANELS->"پنل‌ها";WidgetLayout.STACKED->"ستونی";WidgetLayout.SPLIT->"دوبخشی";WidgetLayout.GLASS->"شیشه‌ای"}
fun widgetContentLabel(c:WidgetContent)=when(c){WidgetContent.SUMMARY->"خلاصه ماه";WidgetContent.TODAY_EXPENSE->"برداشت امروز";WidgetContent.MONTH_EXPENSE->"برداشت ماه";WidgetContent.RECENT->"تراکنش‌های اخیر"}

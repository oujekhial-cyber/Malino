package ir.kharjyar.app.core.nlp

import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.data.prefs.ThemeMode

/** فرمان‌های قطعی تنظیمات؛ محلی اجرا می‌شوند و متن کاربر برای این کارها به سرویس آنلاین نمی‌رود. */
sealed interface SettingsCommand {
 data class Brightness(val mode:ThemeMode):SettingsCommand
 data class Theme(val palette:Palette?,val random:Boolean=false):SettingsCommand
 data class Currency(val unit:MoneyUnit):SettingsCommand
 data class Font(val delta:Int=0,val exact:Int?=null):SettingsCommand
 data object Backup:SettingsCommand
 data class AddCategory(val name:String):SettingsCommand
 data class RenameCategory(val oldName:String,val newName:String):SettingsCommand
 data class ToggleCategory(val name:String,val enabled:Boolean):SettingsCommand
 data class DeleteCategory(val name:String):SettingsCommand
 data object ManageCategories:SettingsCommand
}

object SettingsCommandParser {
 private val themeNames=linkedMapOf(
  "ساکورا" to Palette.SAKURA,"شکوفه" to Palette.SAKURA,"بنفش" to Palette.VIOLET,
  "نیلوفر" to Palette.LOTUS,"اقیانوس" to Palette.OCEAN,"آبی" to Palette.OCEAN,
  "طلایی" to Palette.GOLD,"طلا" to Palette.GOLD,"مینیمال" to Palette.MINIMAL_DAY
 )
 fun parse(text:String):SettingsCommand? {
  val v=Digits.normalizeForMatch(text).replace('‌',' ').replace(Regex("\\s+")," ").trim()
  val change=listOf("کن","بذار","قرار بده","تغییر","عوض","بزرگ","کوچک","بگیر","بساز","اضافه").any(v::contains)
  if(!change)return null
  if((v.contains("بکاپ")||v.contains("پشتیبان"))&&listOf("بگیر","بساز","تهیه").any(v::contains))return SettingsCommand.Backup
  if(v.contains("دسته بندی")||v.contains("دسته‌بندی")) {
   Regex("(?:نام )?(?:دسته ?بندی|دسته‌بندی) ([^،]+?) را به ([^،]+?) تغییر").find(v)?.let{return SettingsCommand.RenameCategory(it.groupValues[1].trim(),it.groupValues[2].trim())}
   Regex("(?:دسته ?بندی|دسته‌بندی) ([^،]+?) را (غیرفعال|فعال)").find(v)?.let{return SettingsCommand.ToggleCategory(it.groupValues[1].trim(),it.groupValues[2]=="فعال")}
   Regex("(?:دسته ?بندی|دسته‌بندی) ([^،]+?) را حذف").find(v)?.let{return SettingsCommand.DeleteCategory(it.groupValues[1].trim())}
   Regex("(?:دسته ?بندی|دسته‌بندی)(?: جدید)?(?: به نام)? ([^،]+?)(?: را)? (?:اضافه|ایجاد|بساز|ثبت)").find(v)?.groupValues?.getOrNull(1)?.trim()?.takeIf{it.isNotBlank()}?.let{return SettingsCommand.AddCategory(it)}
   return SettingsCommand.ManageCategories
  }
  if(v.contains("واحد پول")||v.contains("ریال")||v.contains("تومان"))return when {
   v.contains("تومان")->SettingsCommand.Currency(MoneyUnit.TOMAN)
   v.contains("ریال")->SettingsCommand.Currency(MoneyUnit.RIAL)
   else->null
  }
  if(v.contains("فونت")||v.contains("نوشته")||v.contains("متن"))return when {
   listOf("بزرگ","افزایش","بیشتر").any(v::contains)->SettingsCommand.Font(delta=10)
   listOf("کوچک","کاهش","کمتر").any(v::contains)->SettingsCommand.Font(delta=-10)
   v.contains("استاندارد")->SettingsCommand.Font(exact=100)
   else->null
  }
  if(v.contains("حالت شب")||v.contains("تیره")||v.contains("دارک"))return SettingsCommand.Brightness(ThemeMode.DARK)
  if(v.contains("حالت روز")||v.contains("روشن")||v.contains("لایت"))return SettingsCommand.Brightness(ThemeMode.LIGHT)
  if(v.contains("حالت سیستم")||v.contains("خودکار"))return SettingsCommand.Brightness(ThemeMode.SYSTEM)
  if(v.contains("تم")) {
   themeNames.entries.firstOrNull{v.contains(it.key)}?.let{return SettingsCommand.Theme(it.value)}
   if(v.contains("عوض")||v.contains("تغییر"))return SettingsCommand.Theme(null,random=true)
  }
  return null
 }
}

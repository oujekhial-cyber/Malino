package ir.kharjyar.app

import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.nlp.SettingsCommand
import ir.kharjyar.app.core.nlp.SettingsCommandParser
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.data.prefs.ThemeMode
import org.junit.Assert.*
import org.junit.Test

class SettingsCommandParserTest {
 @Test fun appearanceCommandsAreUnderstoodLocally(){
  assertEquals(SettingsCommand.Brightness(ThemeMode.DARK),SettingsCommandParser.parse("حالت شب را روشن کن"))
  assertEquals(SettingsCommand.Brightness(ThemeMode.LIGHT),SettingsCommandParser.parse("تم رو روشن کن"))
  assertEquals(SettingsCommand.Theme(Palette.SAKURA),SettingsCommandParser.parse("تم را ساکورا کن"))
  assertEquals(SettingsCommand.Theme(null,true),SettingsCommandParser.parse("تم را عوض کن"))
 }
 @Test fun currencyFontBackupAndCategoriesAreUnderstood(){
  assertEquals(SettingsCommand.Currency(MoneyUnit.TOMAN),SettingsCommandParser.parse("واحد پول را تومان کن"))
  assertEquals(SettingsCommand.Font(delta=10),SettingsCommandParser.parse("فونت را بزرگ تر کن"))
  assertEquals(SettingsCommand.Backup,SettingsCommandParser.parse("یک بکاپ بگیر"))
  assertEquals(SettingsCommand.ManageCategories,SettingsCommandParser.parse("دسته بندی ها را تغییر بده"))
  assertEquals(SettingsCommand.AddCategory("تفریح"),SettingsCommandParser.parse("دسته بندی تفریح را اضافه کن"))
  assertEquals(SettingsCommand.RenameCategory("خوراک","سوپرمارکت"),SettingsCommandParser.parse("نام دسته بندی خوراک را به سوپرمارکت تغییر بده"))
  assertEquals(SettingsCommand.ToggleCategory("تفریح",false),SettingsCommandParser.parse("دسته بندی تفریح را غیرفعال کن"))
  assertEquals(SettingsCommand.DeleteCategory("قدیمی"),SettingsCommandParser.parse("دسته بندی قدیمی را حذف کن"))
 }
}

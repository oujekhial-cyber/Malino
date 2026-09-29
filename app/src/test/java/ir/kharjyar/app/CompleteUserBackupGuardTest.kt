package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class CompleteUserBackupGuardTest {
 @Test fun `backup includes every database domain templates private files and settings`() {
  val manager=File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText()
  val payload=File("src/main/java/ir/kharjyar/app/core/backup/BackupPayload.kt").readText()
  listOf("accounts","senders","categories","rules","templates","transactions","transferGroups","smsQueue","blockedSenders","debtPeople","debts","debtPayments","checks","bankBalances","notes","loans","installments","assets","assetTrades","attachments","reminders","profiles","coveredPeople","vehicles","oilServices","civicMessages","privateFiles","settings").forEach{assertTrue("missing backup domain $it",payload.contains("val $it:"))}
  assertTrue(manager.contains("templates = repo.templateDao.allOnce()"))
  assertTrue(manager.contains("privateFiles = collectPrivateFiles"))
  assertTrue(manager.contains("settings = settings.exportForBackup()"))
  assertTrue(manager.contains("settings.importFromBackup(payload.settings)"))
 }
 @Test fun `all app preferences and weather city are round tripped`() {
  val source=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  listOf("theme_mode","palette","app_lock","onboarding_done","widget_palette","secure_screen","digit_style","app_font_scale","dashboard_account_order","weather_city").forEach{assertTrue("missing setting $it",source.substringAfter("exportForBackup").contains("\"$it\""));assertTrue("setting not imported $it",source.substringAfter("importFromBackup").contains("\"$it\""))}
 }
}

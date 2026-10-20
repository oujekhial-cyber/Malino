package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReminderNotificationNavigationGuardTest {
 @Test fun `life reminder notification opens and focuses its reminder`() {
  val worker=File("src/main/java/ir/kharjyar/app/work/LifeReminderWorker.kt").readText()
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/RemindersScreen.kt").readText()
  assertTrue(worker.contains("reminders/${'$'}{reminder.id}"))
  assertTrue(root.contains("composable(\"reminders/{focusId}\")"))
  assertTrue(screen.contains("sortedByDescending{it.id==focusId}"))
  assertTrue(screen.contains("یادآور بازشده از اعلان"))
 }
 @Test fun `all due obligation notifications have actionable destinations`() {
  val worker=File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").readText()
  assertTrue(worker.contains("debts.isNotEmpty()->\"debts\""))
  assertTrue(worker.contains("checks.isNotEmpty()->\"checks\""))
  assertTrue(worker.contains("installments.isNotEmpty()->\"loans\""))
  assertTrue(worker.contains("setContentIntent(Notifier.routePendingIntent"))
 }
 @Test fun `global fast drawer interception is removed`() {
  val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
  val rows=File("src/main/java/ir/kharjyar/app/ui/components/SwipeActions.kt").readText()
  assertFalse(root.contains("fastDrawerGesture"))
  assertFalse(root.contains("fastDrawerVelocity"))
  assertFalse(rows.contains("fastDrawerVelocity"))
 }
}

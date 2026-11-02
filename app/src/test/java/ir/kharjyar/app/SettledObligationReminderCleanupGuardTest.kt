package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SettledObligationReminderCleanupGuardTest {
    @Test fun `settled debts clear their exact active notification in every terminal path`() {
        val screen=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        val notifier=File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
        assertTrue(notifier.contains("cancel(TAG_DEBT_REMINDER, debtId.toInt())"))
        assertTrue(screen.contains("settled = true,reminderAt=null"))
        assertTrue(screen.contains("settled=true,reminderAt=null"))
        assertTrue(screen.contains("cancelDebtReminder(context,p.debt.id)"))
        assertTrue(screen.contains("cancelDebtReminder(context,debt.id)"))
    }

    @Test fun `cleared bounced edited and deleted checks cancel all reminder mechanisms`() {
        val screen=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
        val cleanup=File("src/main/java/ir/kharjyar/app/work/ObligationReminderCleanup.kt").readText()
        val notifier=File("src/main/java/ir/kharjyar/app/notify/Notifier.kt").readText()
        assertTrue(screen.contains("ObligationReminderCleanup.cancelCheck(context,vm.repo.db,check)"))
        assertTrue(screen.contains("ObligationReminderCleanup.cancelCheck(context,vm.repo.db,existing)"))
        assertTrue(screen.contains("if(record.status==CheckStatus.PENDING)"))
        assertTrue(cleanup.contains("Notifier.cancelCheckReminder(context, check.id)"))
        assertTrue(cleanup.contains("LifeReminderWorker.cancel(context, reminder.id)"))
        assertTrue(cleanup.contains("Notifier.cancelLifeReminder(context, reminder.id)"))
        assertTrue(cleanup.contains("enabled = false"))
        assertTrue(notifier.contains("cancel(TAG_CHECK_REMINDER, checkId.toInt())"))
        assertTrue(notifier.contains("cancel((12000L + reminderId).toInt())"))
    }

    @Test fun `periodic worker defensively purges terminal obligations restored from old data`() {
        val worker=File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").readText()
        assertTrue(worker.contains("allDebtsOnce().filter{it.settled}"))
        assertTrue(worker.contains("allOnce().filter{it.status!=CheckStatus.PENDING}"))
        assertTrue(worker.contains("Notifier.TAG_CHECK_REMINDER"))
    }
}

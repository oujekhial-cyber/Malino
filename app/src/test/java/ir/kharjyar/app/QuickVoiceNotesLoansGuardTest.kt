package ir.kharjyar.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class QuickVoiceNotesLoansGuardTest {
 @Test fun `quick add automatically launches system speech recognition`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText();assertTrue(s.contains("LaunchedEffect(voiceAvailable)"));assertTrue(s.contains("if (voiceAvailable) startVoice()"));assertTrue(s.contains("fa-IR")) }
 @Test fun `home header has no weather`() { val s=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertFalse(s.contains("WeatherService"));assertFalse(s.contains("weather?.let")) }
 @Test fun `notes are persisted and exposed in sidebar`() { val db=File("src/main/java/ir/kharjyar/app/data/db/PlanningEntities.kt").readText();val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertTrue(db.contains("NoteEntity"));assertTrue(root.contains("یادداشت‌ها"));assertTrue(root.contains("NotesScreen")) }
 @Test fun `loans generate installments with due reminders`() { val screen=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText();val worker=File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").readText();assertTrue(screen.contains("insertInstallments"));assertTrue(screen.contains("ثبت پرداخت قسط"));assertTrue(worker.contains("loanDao().due")) }
}

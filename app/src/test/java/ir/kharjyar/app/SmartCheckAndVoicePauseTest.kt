package ir.kharjyar.app

import ir.kharjyar.app.ui.screens.parseCheckBank
import ir.kharjyar.app.ui.screens.parseCheckDraftDate
import ir.kharjyar.app.ui.screens.parseCheckParty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmartCheckAndVoicePauseTest {
    @Test fun `spoken check date bank and party are extracted locally`() {
        val text="یک چک از علی رضایی به مبلغ ده میلیون بانک ملت سررسید ۱۴۰۵/۰۸/۱۲ ثبت کن"
        assertEquals("بانک ملت", parseCheckBank(text))
        assertEquals("علی رضایی", parseCheckParty(text))
        assertEquals("1405/08/12", parseCheckDraftDate(null,text)?.format(false))
    }

    @Test fun `AI check fields reach the actual check editor`() {
        val checks=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
        val ai=File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
        assertTrue(checks.contains("pending?.takeIf{it.action==\"check_issued\"||it.action==\"check_received\"}"))
        listOf("dueDate","sayadId","serialNumber","bankName","nationalId","chequeAccountNumber","chequeIban").forEach { assertTrue(ai.contains("value[\"$it\"]")) }
        assertTrue(checks.contains("draft?.amountRial"))
        assertTrue(checks.contains("parseCheckDraftDate"))
    }

    @Test fun `speech recognizer allows multi second pauses`() {
        val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
        assertTrue(quick.contains("EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L"))
        assertTrue(quick.contains("EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3_500L"))
        assertTrue(quick.contains("EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15_000L"))
    }
}

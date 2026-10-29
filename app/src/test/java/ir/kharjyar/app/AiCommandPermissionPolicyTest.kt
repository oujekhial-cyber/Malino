package ir.kharjyar.app

import ir.kharjyar.app.core.nlp.AiCommandPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCommandPermissionPolicyTest {
    @Test fun `read and navigation commands can run immediately`() {
        assertTrue(AiCommandPolicy.canExecuteImmediately("navigate"))
        assertTrue(AiCommandPolicy.canExecuteImmediately("transaction_query"))
        assertTrue(AiCommandPolicy.canExecuteImmediately("report_query"))
    }

    @Test fun `all persistent writes require one human confirmation`() {
        listOf("transaction","loan_borrowed","debt_receivable","asset_record","reminder_create","check_issued","note_create","vehicle_record","installment_payment").forEach {
            assertTrue(it,AiCommandPolicy.requiresConfirmation(it))
            assertFalse(it,AiCommandPolicy.canExecuteImmediately(it))
        }
    }

    @Test fun `destructive or secret operations are forbidden`() {
        listOf("delete","bulk_delete","change_balance","execute_code","export_secret").forEach {
            assertFalse(AiCommandPolicy.isAllowed(it))
        }
    }
}

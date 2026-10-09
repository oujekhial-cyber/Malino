package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AccountEditCancelGuardTest {
    @Test fun `account form offers explicit cancel without saving changes`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
        assertTrue(source.contains("onClick = { nav.popBackStack() }"))
        assertTrue(source.contains("Text(if (accountId > 0) \"لغو ویرایش\" else \"انصراف\")"))
    }
}

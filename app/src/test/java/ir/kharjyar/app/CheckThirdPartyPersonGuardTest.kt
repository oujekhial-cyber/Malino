package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CheckThirdPartyPersonGuardTest {
    @Test fun `check person can explicitly differ from financial counterparty`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
        listOf(
            "طرف حساب اصلی *",
            "differentChequePerson",
            "صادرکننده چک شخص دیگری است",
            "دریافت‌کننده چک شخص دیگری است",
            "نام صادرکننده چک *",
            "نام دریافت‌کننده چک *",
            "if(differentChequePerson)issuer.trim() else party.trim()",
            "if(differentChequePerson)receiver.trim() else party.trim()",
            "!differentChequePerson||(if(received)issuer else receiver).isNotBlank()"
        ).forEach { assertTrue(it, source.contains(it)) }
        assertFalse(source.contains("issuer=it;party=it"))
        assertFalse(source.contains("receiver=it;party=it"))
    }
}

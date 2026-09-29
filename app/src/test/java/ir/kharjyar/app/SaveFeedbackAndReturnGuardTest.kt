package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class SaveFeedbackAndReturnGuardTest {
 @Test fun `major registration forms confirm save before returning`(){val forms=listOf("DebtsScreen.kt","ChecksScreen.kt","LoansScreen.kt","RemindersScreen.kt","CivicCenterScreen.kt","VehiclesScreen.kt","AssetsScreen.kt");forms.forEach{name->val s=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText();assertTrue(name,s.contains("showSavedMessage"));assertTrue(name,s.indexOf("showSavedMessage",s.indexOf("showSavedMessage")+1)>0)}}
 @Test fun `profile transaction and account saves confirm then pop when workflow is complete`(){val profile=File("src/main/java/ir/kharjyar/app/ui/screens/ProfileScreen.kt").readText();assertTrue(profile.contains("showSavedMessage(context,\"پروفایل\");nav.popBackStack()"));val tx=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText();assertTrue(tx.indexOf("showSavedMessage(context, \"تراکنش\")")<tx.indexOf("nav.popBackStack()",tx.indexOf("showSavedMessage(context, \"تراکنش\")")));val account=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText();assertTrue(account.contains("showSavedMessage(context, \"حساب\")"))}
 @Test fun `shared success message is explicit`(){val s=File("src/main/java/ir/kharjyar/app/ui/components/RegistrationFeedback.kt").readText();assertTrue(s.contains("با موفقیت ثبت شد"))}
}

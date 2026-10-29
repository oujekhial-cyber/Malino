package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ListFirstBackNavigationGuardTest {
 @Test fun `registration and detail subpages consume back and reveal their own list`(){val expected=listOf(Triple("DebtsScreen.kt","BackHandler","entryKind"),Triple("ChecksScreen.kt","BackHandler","entryDirection"),Triple("LoansScreen.kt","BackHandler","entryKind"),Triple("RemindersScreen.kt","BackHandler","entryRepeat"),Triple("CivicCenterScreen.kt","BackHandler","entry"),Triple("VehiclesScreen.kt","BackHandler","entry"),Triple("AssetsScreen.kt","BackHandler","adding"));expected.forEach{(name,handler,state)->val source=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText();assertTrue(name,source.contains(handler)&&source.contains(state))}}
 @Test fun `back navigation waits for release instead of predictive preview`(){val manifest=File("src/main/AndroidManifest.xml").readText();assertTrue(manifest.contains("android:enableOnBackInvokedCallback=\"false\""))}
}

package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ListFirstBackNavigationGuardTest {
 @Test fun `registration and detail subpages consume back and reveal their own list`(){val expected=mapOf("DebtsScreen.kt" to "BackHandler" to "entryKind", "ChecksScreen.kt" to "BackHandler" to "entryDirection", "LoansScreen.kt" to "BackHandler" to "entryKind", "RemindersScreen.kt" to "BackHandler" to "entryRepeat", "CivicCenterScreen.kt" to "BackHandler" to "entry", "VehiclesScreen.kt" to "BackHandler" to "entry", "AssetsScreen.kt" to "BackHandler" to "adding");expected.forEach{entry->val name=entry.first.first;val source=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText();assertTrue(name,source.contains(entry.first.second)&&source.contains(entry.second))}}
 @Test fun `back navigation waits for release instead of predictive preview`(){val manifest=File("src/main/AndroidManifest.xml").readText();assertTrue(manifest.contains("android:enableOnBackInvokedCallback=\"false\""))}
}

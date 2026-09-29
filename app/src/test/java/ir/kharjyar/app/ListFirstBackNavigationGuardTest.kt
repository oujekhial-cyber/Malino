package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class ListFirstBackNavigationGuardTest {
 @Test fun `registration subpages consume first back and reveal their own list`(){val expected=mapOf("DebtsScreen.kt" to "BackHandler(enabled=entryKind", "ChecksScreen.kt" to "BackHandler(enabled=entryDirection", "LoansScreen.kt" to "BackHandler(enabled=entryKind", "RemindersScreen.kt" to "BackHandler(enabled=entryRepeat", "CivicCenterScreen.kt" to "BackHandler(enabled=entry", "VehiclesScreen.kt" to "BackHandler(enabled=entry", "AssetsScreen.kt" to "BackHandler(enabled=adding||editor");expected.forEach{(name,guard)->assertTrue(name,File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText().contains(guard))}}
 @Test fun `back navigation waits for release instead of predictive preview`(){val manifest=File("src/main/AndroidManifest.xml").readText();assertTrue(manifest.contains("android:enableOnBackInvokedCallback=\"false\""))}
}

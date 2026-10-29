package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class AssetModernGraphicsGuardTest {
 @Test fun `asset categories and rows use modern semantic graphics instead of letters`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText()
  listOf("AssetKindGraphic","Icons.Filled.DirectionsCar","Icons.Filled.HomeWork","Icons.Filled.ViewInAr","Icons.Filled.AutoAwesomeMosaic","RoundedCornerShape(17.dp)","AssetKindGraphic(asset.kind,accent)").forEach{assertTrue(it,source.contains(it))}
  assertFalse(source.contains("AssetKind.GOLD->\"ط\""))
  assertFalse(source.contains("AssetKind.VEHICLE->\"خ\""))
 }
}

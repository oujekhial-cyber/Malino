package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** نگهبان تصمیم جدید: رهگیری سراسری Flick حذف شده و کشو از رفتار استاندارد Material استفاده می‌کند. */
class FastGlobalDrawerGestureGuardTest {
 private val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
 private val swipe=File("src/main/java/ir/kharjyar/app/ui/components/SwipeActions.kt").readText()

 @Test fun `global fast flick interception stays removed`() {
  assertFalse(root.contains("fastDrawerGesture"))
  assertFalse(root.contains("fastDrawerVelocity"))
  assertFalse(root.contains("PointerEventPass.Initial"))
  assertTrue(root.contains("ModalNavigationDrawer("))
  assertTrue(root.contains("modifier = Modifier,"))
 }

 @Test fun `normal drawer controls remain available`() {
  assertTrue(root.contains("if (drawerState.isOpen) drawerState.close() else drawerState.open()"))
  assertTrue(root.contains("BackHandler(enabled = drawerState.isOpen)"))
 }

 @Test fun `row actions use their normal distance threshold without reserved velocity`() {
  assertTrue(swipe.contains("onDragStopped = { _ ->"))
  assertFalse(swipe.contains("fastDrawerVelocity"))
  assertTrue(swipe.contains("settled <= -trigger -> onDelete()"))
  assertTrue(swipe.contains("settled >= trigger -> onEdit()"))
 }
}

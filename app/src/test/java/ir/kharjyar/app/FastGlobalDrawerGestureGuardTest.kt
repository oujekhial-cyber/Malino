package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FastGlobalDrawerGestureGuardTest {
    private val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText()
    private val swipe=File("src/main/java/ir/kharjyar/app/ui/components/SwipeActions.kt").readText()

    @Test fun `fast drawer gesture wraps every screen and observes before children`() {
        assertTrue(root.contains("Modifier.pointerInput(drawerState.isClosed, rootLayoutDirection)"))
        assertTrue(root.contains("awaitPointerEvent(PointerEventPass.Initial)"))
        assertTrue(root.contains("modifier = fastDrawerGesture"))
        assertTrue(root.contains("drawerState.open()"))
        assertTrue(root.contains("1100.dp.toPx()"))
        assertTrue(root.contains("36.dp.toPx()"))
    }

    @Test fun `drawer flick is directional and predominantly horizontal`() {
        assertTrue(root.contains("rootLayoutDirection == LayoutDirection.Rtl"))
        assertTrue(root.contains("velocity.x <= -fastDrawerVelocity"))
        assertTrue(root.contains("kotlin.math.abs(velocity.x) > kotlin.math.abs(velocity.y) * 1.35f"))
    }

    @Test fun `fast swipe never triggers row edit or delete`() {
        assertTrue(swipe.contains("onDragStopped = { velocity ->"))
        assertTrue(swipe.contains("if (abs(velocity) < fastDrawerVelocity)"))
        assertTrue(swipe.contains("settled <= -trigger -> onDelete()"))
        assertTrue(swipe.contains("settled >= trigger -> onEdit()"))
    }
}

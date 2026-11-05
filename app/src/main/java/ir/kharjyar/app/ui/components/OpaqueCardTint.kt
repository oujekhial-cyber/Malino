package ir.kharjyar.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/** Pre-blend the tint over an opaque surface so elevated cards never reveal their own shadow. */
fun opaqueCardTint(accent: Color, surface: Color, alpha: Float): Color =
    accent.copy(alpha = alpha.coerceIn(0f, 1f)).compositeOver(surface.copy(alpha = 1f))

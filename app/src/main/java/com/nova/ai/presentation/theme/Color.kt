package com.nova.ai.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// অফিসিয়াল NOVA AI কালার প্যালেট
val NovaBackground = Color(0xFF080B14)
val NovaSecondaryBackground = Color(0xFF101625)
val NovaCardBackground = Color(0xFF151D30)
val NovaCardElevated = Color(0xFF1C263F)

val NovaPrimaryPurple = Color(0xFF8157FF)
val NovaSecondaryBlue = Color(0xFF3587FF)
val NovaAccentCyan = Color(0xFF37D9E8)

val NovaPrimaryText = Color(0xFFFFFFFF)
val NovaSecondaryText = Color(0xFFA8B0C3)
val NovaMutedText = Color(0xFF64748B)

val NovaSuccess = Color(0xFF25C989)
val NovaError = Color(0xFFFF5268)
val NovaWarning = Color(0xFFFFB020)
val NovaBorder = Color(0xFF222E4A)

// গ্রেডিয়েন্টস
val NovaBrandGradient = Brush.horizontalGradient(
    colors = listOf(NovaPrimaryPurple, NovaSecondaryBlue, NovaAccentCyan)
)

val NovaOrbGradient = Brush.radialGradient(
    colors = listOf(
        NovaAccentCyan.copy(alpha = 0.8f),
        NovaPrimaryPurple.copy(alpha = 0.6f),
        Color.Transparent
    )
)

val NovaCardGradient = Brush.verticalGradient(
    colors = listOf(
        NovaCardBackground,
        NovaSecondaryBackground
    )
)

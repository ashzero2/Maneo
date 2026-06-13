package com.maneo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maneo.app.R

// Spec §7 — Lora for verse/display text (warm serif, fits scripture)
// Bundled as TTF assets — no GMS/network dependency, works offline from first launch
val Lora: FontFamily = FontFamily(
    Font(R.font.lora_regular,  FontWeight.Normal),
    Font(R.font.lora_medium,   FontWeight.Medium),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
)

// Spec §7 — Inter for all body/UI text (clean, universally legible)
val Inter: FontFamily = FontFamily(
    Font(R.font.inter_regular,  FontWeight.Normal),
    Font(R.font.inter_medium,   FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val ManeoTypography = Typography(
    // Lora — display and headline styles (verses, screen titles)
    displayLarge   = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.Normal,   fontSize = 32.sp, lineHeight = 40.sp),
    displayMedium  = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.Normal,   fontSize = 28.sp, lineHeight = 36.sp),
    displaySmall   = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.Normal,   fontSize = 24.sp, lineHeight = 32.sp),
    headlineLarge  = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontFamily = Lora,  fontWeight = FontWeight.Medium,   fontSize = 18.sp, lineHeight = 26.sp),

    // Inter — title, body, and label styles (UI elements, journal text)
    titleLarge     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)

package com.luke.pager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.luke.pager.R

private val gentiumProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

private val GentiumFontFamily =
    FontFamily(
        Font(googleFont = GoogleFont("Gentium Book Plus"), fontProvider = gentiumProvider, weight = FontWeight.Normal),
        Font(googleFont = GoogleFont("Gentium Book Plus"), fontProvider = gentiumProvider, weight = FontWeight.Medium),
        Font(googleFont = GoogleFont("Gentium Book Plus"), fontProvider = gentiumProvider, weight = FontWeight.Bold)
    )

val Typography =
    Typography(
        bodyLarge =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodySmall =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium =
        TextStyle(
            fontFamily = GentiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )

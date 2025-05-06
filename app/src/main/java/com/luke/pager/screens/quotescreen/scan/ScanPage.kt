package com.luke.pager.screens.quotescreen.scan

import android.graphics.Bitmap
import android.net.Uri

data class ScanPage(
    val imageUri: Uri,
    val rotatedBitmap: Bitmap
)
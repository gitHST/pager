package com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses

import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.text.Text

data class ScanPage(
    val imageUri: Uri,
    val rotatedBitmap: Bitmap,
    val allClusters: List<List<Text.TextBlock>>,
    val imageWidth: Int,
    val imageHeight: Int,
)

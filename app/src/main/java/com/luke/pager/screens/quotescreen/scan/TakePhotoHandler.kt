package com.luke.pager.screens.quotescreen.scan

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.luke.pager.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun takePhotoHandler(
    snackbarScope: CoroutineScope,
    onPhotoCaptured: (Uri) -> Unit,
    testMode: Boolean = false
): () -> Unit {
    val context = LocalContext.current
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var testImageCounter by rememberSaveable { mutableIntStateOf(2) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            lastPhotoUri?.let { onPhotoCaptured(it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                val shouldShowRationale = when (context) {
                    is Activity -> ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.CAMERA
                    )
                    else -> false
                }

                if (!shouldShowRationale) {
                    snackbarScope.launch {
                        snackbarScope.launch {
                        }
                    }
                } else {
                    snackbarScope.launch {
                        snackbarScope.launch {
                        }
                    }
                }
            }
        }
    )

    return remember {
        {
            if (testMode) {
                val testImageName = when (testImageCounter) {
                    2 -> R.drawable.sample_text_image_two
                    3 -> R.drawable.sample_text_image_three
                    4 -> R.drawable.sample_text_image_four
                    5 -> R.drawable.sample_text_image_five
                    else -> R.drawable.sample_text_image_two
                }
                val testImageUri = "android.resource://${context.packageName}/$testImageName".toUri()
                onPhotoCaptured(testImageUri)
                testImageCounter = if (testImageCounter >= 5) 2 else testImageCounter + 1
            } else {
                val permissionCheck = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                )
                if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val photoFile = File(
                        context.cacheDir,
                        "${UUID.randomUUID()}.jpg"
                    )
                    val newPhotoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        photoFile
                    )
                    lastPhotoUri = newPhotoUri
                    cameraLauncher.launch(newPhotoUri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

package com.luke.pager.screens.quotescreen.scan

import android.Manifest
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
                snackbarScope.launch {
                }
            }
        }
    )

    fun launchRealCamera() {
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
                val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                    context.resources,
                    testImageName,
                    context.theme
                )
                if (drawable != null) {
                    val testImageUri =
                        "android.resource://${context.packageName}/$testImageName".toUri()
                    onPhotoCaptured(testImageUri)
                    testImageCounter = if (testImageCounter >= 5) 2 else testImageCounter + 1
                } else {
                    launchRealCamera()
                }
            } else {
                launchRealCamera()
            }
        }
    }
}

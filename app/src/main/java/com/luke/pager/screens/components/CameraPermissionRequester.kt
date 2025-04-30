package com.luke.pager.screens.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun RequestCameraPermissionResult(onResult: (PermissionResult) -> Unit) {
    val context = LocalContext.current
    var hasAttemptedRequest by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onResult(PermissionResult.Granted)
        } else {
            val activity = context.findActivityOrNull()
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA) ?: false
            if (shouldShowRationale) {
                onResult(PermissionResult.DeniedTemporarily)
            } else {
                onResult(PermissionResult.DeniedPermanently)
            }
        }
    }

    LaunchedEffect(Unit) {
        val currentStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (currentStatus == PackageManager.PERMISSION_GRANTED) {
            onResult(PermissionResult.Granted)
        } else if (!hasAttemptedRequest) {
            hasAttemptedRequest = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}


fun Context.findActivityOrNull(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun Activity.shouldShowRequestPermissionRationaleCompat(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

sealed class PermissionResult {
    object Granted : PermissionResult()
    object DeniedTemporarily : PermissionResult()
    object DeniedPermanently : PermissionResult()
}
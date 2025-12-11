package com.luke.pager.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun canSyncNow(
    context: Context,
    allowCellular: Boolean,
): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

    val onWifiLike =
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

    val onCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

    return when {
        onWifiLike -> true
        onCellular -> allowCellular
        else -> false
    }
}

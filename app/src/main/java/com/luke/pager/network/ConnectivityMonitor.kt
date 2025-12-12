package com.luke.pager.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

fun onlineStatusFlow(
    context: Context,
    intervalMs: Long = 10_000L,
): Flow<Boolean> = flow {
    while (true) {
        emit(isOnlineNow(context))
        delay(intervalMs)
    }
}.distinctUntilChanged()

fun isOnlineNow(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false

    if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false

    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

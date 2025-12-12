package com.luke.pager.auth

fun mapAuthErrorToUserMessage(raw: String?): String {
    if (raw.isNullOrBlank()) {
        return "Sorry, please try again later"
    }

    val msg = raw.lowercase()

    return when {
        "password" in msg && "incorrect" in msg ->
            "Incorrect email or password"

        "sign-in fail" in msg ->
            "Please try again"

        "no user record" in msg || "user not found" in msg ->
            "No account found with that email"

        "email address is badly formatted" in msg ->
            "Please enter a valid email address"

        "already in use" in msg ->
            "An account with this email already exists"

        "network" in msg || "timeout" in msg ->
            "You're offline. Please check your connection"

        else ->
            "Sorry, please try again later"
    }
}

# Keep generic type info for Gson
-keepattributes Signature
-keepattributes *Annotation*

# Keep all data classes and Retrofit interfaces
-keep class com.luke.pager.network.** { *; }

# Keep Gson internals
-keep class com.google.gson.** { *; }

# Keep Retrofit HTTP annotations and method signatures
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep SerializedName annotations (if used)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep generic type info for Gson
-keepattributes Signature
-keepattributes *Annotation*

# Keep your own network data classes and Retrofit interfaces
# (adjust package paths if your models/APIs live elsewhere)
-keep class com.luke.pager.network.** { *; }

# If you use Gson's TypeToken, keep that specific class
-keep class com.google.gson.reflect.TypeToken

# Keep Retrofit HTTP annotations and method signatures
-keepclassmembers class com.luke.pager.network.** {
    @retrofit2.http.* <methods>;
}

# Keep SerializedName-annotated fields on your network models
-keepclassmembers class com.luke.pager.network.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

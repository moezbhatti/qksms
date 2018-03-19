-dontobfuscate

# android-smsmms
# -keep class android.net.** { *; }
-dontwarn android.net.ConnectivityManager
-dontwarn android.net.LinkProperties

# okio
-dontwarn okio.**
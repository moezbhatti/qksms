-dontobfuscate

#
# To fix an error to do with android.location.Country. We may be able to optimize
# more parts of the android.location package.
#
-keep class android.location.** { *; }

#
# See com.moez.QKSMS.ui.mms.PresenterFactory---it uses reflect
#
-keep class com.moez.QKSMS.ui.mms.MmsThumbnailPresenter { *; }
-keep class com.moez.QKSMS.ui.mms.SlideshowPresenter { *; }
-keep class com.android.internal.util.ArrayUtils { *; }

#
# Klinker's MMS code uses reflection on android.net.ConnectivityManager (and subclasses).
#
-keep class android.net.** { *; }

#
# SlidingMenu
#
-keep class android.view.Display { *; }

#
# SystemBarTint
#
-keep class android.os.SystemProperties { *; }

#
# libphonenumber
#
-keep class com.google.i18n.phonenumbers.** { *; }

#
# Android Volley
# see: http://stackoverflow.com/questions/21816643/volley-seems-not-working-after-proguard-obfuscate
#
-keep class org.apache.commons.logging.** { *; }

#
# Google Play Services proguard
# see: http://developer.android.com/google/play-services/setup.html#Proguard
#
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#
# Crittercism
# see: http://docs.crittercism.com/android/android.html
#
-keep public class com.crittercism.**
-keepclassmembers public class com.crittercism.* {
    *;
}

# To get line numbers, source files from Crittercism:
-keepattributes SourceFile, LineNumberTable

# ez-vcard
# https://github.com/nickel-chrome/CucumberSync/blob/master/proguard-project.txt
-dontwarn com.fasterxml.jackson.**		# Jackson JSON Processor (for jCards) not used
-dontwarn freemarker.**				# freemarker templating library (for creating hCards) not used
-dontwarn org.jsoup.**				# jsoup library (for hCard parsing) not used
-dontwarn sun.misc.Perf
-keep class ezvcard.property.** { *; }		# keep all VCard properties (created at runtime)

# ButterKnife
# http://jakewharton.github.io/butterknife/
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# RetroLambda
# https://github.com/evant/gradle-retrolambda
-dontwarn java.lang.invoke.*

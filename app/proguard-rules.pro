# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString


# Ditto rules
#-keep class live.ditto.internal.swig.ffi.** { *; }
#-keep class live.ditto.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keep class live.ditto.* { *; }
-keep class live.ditto.transports.** { *; }
-keep class live.ditto.internal.** { *; }

-keepnames class live.ditto.tools.** { *; }

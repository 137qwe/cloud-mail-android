# Retrofit / Gson / OkHttp / Room / Hilt 默认规则（minify 开启时按需补充）
-keepattributes Signature
-keepattributes *Annotation*

# Gson 泛型
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Retrofit
-keepattributes InnerClasses,EnclosingMethod
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# Room
-keep class * extends androidx.room.RoomDatabase

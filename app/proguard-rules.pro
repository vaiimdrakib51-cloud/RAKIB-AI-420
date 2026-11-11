# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Dagger Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.internal.ComponentManager
-keep class androidx.hilt.lifecycle.ViewModelFactoryModules.** { *; }

# Google Generative AI (Gemini SDK)
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# DataStore & Serialization
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }
-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# Keep Retrofit & Gson models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

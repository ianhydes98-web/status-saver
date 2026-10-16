# Keep data classes
-keep class com.statussaver.app.data.model.** { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

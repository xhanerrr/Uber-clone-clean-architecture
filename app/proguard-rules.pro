-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

-keep class * extends androidx.activity.ComponentActivity
-keep class * extends androidx.fragment.app.Fragment

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
# Keep Gson serialized fields and data models for Retrofit/Gson deserialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class de.knollfrank.extensionsformaps.optimize.ors.** { *; }
-keep class de.knollfrank.extensionsformaps.optimize.osrm.** { *; }

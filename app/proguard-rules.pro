# Room - keep entity classes and DAO methods
-keep class com.stopwatch.app.data.model.** { *; }
-keep class com.stopwatch.app.data.dao.** { *; }

# Room generated code
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

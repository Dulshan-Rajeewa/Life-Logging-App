package com.example.lifelogger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// We tell Room that this database uses our LifeLogEntry table
@Database(entities = [LifeLogEntry::class, User::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // This links to the DAO interface we wrote earlier
    abstract fun logDao(): LogDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If the database already exists, return it. If not, build it.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_logger_offline_db"
                ).fallbackToDestructiveMigration() // Clear data if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
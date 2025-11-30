package com.example.alcabolt.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TextEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textEntryDao(): TextEntryDao
}
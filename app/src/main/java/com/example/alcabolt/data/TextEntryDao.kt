package com.example.alcabolt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TextEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TextEntry)

    @Query("SELECT * FROM text_history ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<TextEntry>>

    @Query("DELETE FROM text_history WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}
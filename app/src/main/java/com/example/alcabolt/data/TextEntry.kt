package com.example.alcabolt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TextEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalText: String,
    val translatedText: String,
    val sourceLangCode: String,
    val targetLangCode: String,
    val timestamp: Long = System.currentTimeMillis() // Saves creation time
)
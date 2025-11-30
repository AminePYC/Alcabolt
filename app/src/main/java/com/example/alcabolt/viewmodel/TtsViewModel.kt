package com.example.alcabolt.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alcabolt.data.TextEntry
import com.example.alcabolt.data.TextEntryDao
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Locale

class TtsViewModel(
    private val appTts: TextToSpeech,
    private val dao: TextEntryDao
) : ViewModel() {

    // --- State Management (Used by InputScreen) ---
    var originalText by mutableStateOf("")
        private set
    var translatedText by mutableStateOf("")
        private set
    var isTranslating by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set

    // --- Data Persistence (Used by HistoryScreen) ---
    // 🚨 FIX: This public flow is what HistoryScreen needs to collect its data.
    val historyEntries: Flow<List<TextEntry>> = dao.getAllEntries()

    // --- Translation Setup ---
    // Available languages (simplified list for demonstration)
    val sourceLanguage by mutableStateOf(TranslateLanguage.ENGLISH)
    val targetLanguage by mutableStateOf(TranslateLanguage.FRENCH)

    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
    )

    init {
        downloadTranslationModels()

        appTts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isSpeaking = true }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            override fun onError(utteranceId: String?) { isSpeaking = false; statusMessage = "TTS Error." }
        })
    }

    // --- Data Handlers ---

    fun onTextChange(newText: String) {
        originalText = newText
        translatedText = ""
    }

    fun deleteEntry(entry: TextEntry) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteEntry(entry.id)
        statusMessage = "Entry deleted."
    }

    // --- Core Functions ---

    private fun downloadTranslationModels() = viewModelScope.launch {
        try {
            statusMessage = "Downloading language models..."
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()
            statusMessage = "Language models are ready."
        } catch (e: Exception) {
            statusMessage = "Model download failed: ${e.message}"
        }
    }

    fun translateAndSpeak() = viewModelScope.launch {
        if (originalText.isBlank()) {
            statusMessage = "Please enter text to translate."
            return@launch
        }
        isTranslating = true
        statusMessage = "Translating..."
        try {
            translatedText = translator.translate(originalText).await()
            statusMessage = "Translation complete."
            speakText(translatedText)

            saveEntry(originalText, translatedText, sourceLanguage, targetLanguage)

        } catch (e: Exception) {
            statusMessage = "Translation failed: ${e.message}"
        } finally {
            isTranslating = false
        }
    }

    fun speakOriginal() {
        if (originalText.isBlank()) {
            statusMessage = "Please enter text to speak."
            return
        }
        speakText(originalText)
        translatedText = ""
    }

    fun stopSpeaking() {
        appTts.stop()
        isSpeaking = false
    }

    // This function is public for external UI access (HistoryCard)
    fun speakText(text: String) {
        val locale = Locale.forLanguageTag(TranslateLanguage.toLanguageTag(targetLanguage))
        appTts.language = locale
        appTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTT_ID_SPEAK")
        isSpeaking = true
    }

    private fun saveEntry(
        original: String,
        translated: String,
        source: String,
        target: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val entry = TextEntry(
            originalText = original,
            translatedText = translated,
            sourceLangCode = source,
            targetLangCode = target
        )
        dao.insertEntry(entry)
    }

    fun exportAudio(context: Context, text: String, fileName: String) {
        if (isSpeaking) {
            statusMessage = "Please stop the current speech before exporting."
            return
        }

        val audioFile = File(context.getExternalFilesDir(null), "$fileName.wav")

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UTT_ID_EXPORT")

        val result = appTts.synthesizeToFile(text, params, audioFile, "UTT_ID_EXPORT")

        if (result == TextToSpeech.SUCCESS) {
            statusMessage = "Audio saved successfully to: ${audioFile.absolutePath}"
        } else {
            statusMessage = "Audio export failed (Error Code: $result)"
        }
    }

    override fun onCleared() {
        appTts.stop()
        appTts.shutdown()
        translator.close()
        super.onCleared()
    }
}
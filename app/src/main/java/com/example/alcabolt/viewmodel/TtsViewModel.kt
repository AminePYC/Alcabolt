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

    // --- State Management (InputScreen) ---
    var originalText by mutableStateOf("")
        private set
    var translatedText by mutableStateOf("")
        private set
    var isTranslating by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    // FIX: Removed 'private set' so InputScreen can clear the message.
    var statusMessage by mutableStateOf<String?>(null)

    // --- Data Persistence (HistoryScreen) ---
    val historyEntries: Flow<List<TextEntry>> = dao.getAllEntries()

    // --- Language Configuration ---

    // NEW: Map of display names to ML Kit codes
    val supportedLanguages = mapOf(
        "English" to TranslateLanguage.ENGLISH,
        "French" to TranslateLanguage.FRENCH,
        "Spanish" to TranslateLanguage.SPANISH,
        "German" to TranslateLanguage.GERMAN,
        "Arabic" to TranslateLanguage.ARABIC,
        "Chinese" to TranslateLanguage.CHINESE,
        "Russian" to TranslateLanguage.RUSSIAN,
        "Japanese" to TranslateLanguage.JAPANESE
    )

    // CHANGE: Make mutable state variables for language selection
    var sourceLanguage by mutableStateOf(TranslateLanguage.ENGLISH)
    var targetLanguage by mutableStateOf(TranslateLanguage.FRENCH)

    // NEW: Function to create a new translator client
    private fun createTranslator() = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
    )

    private var translator = createTranslator()

    init {
        downloadTranslationModels()

        appTts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isSpeaking = true }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            override fun onError(utteranceId: String?) { isSpeaking = false; statusMessage = "TTS Error." }
        })
    }

    // NEW: Function to handle language changes
    fun onLanguageChange(isSource: Boolean, newLangCode: String) {
        // Prevent setting the same language for source and target
        if (isSource && newLangCode == targetLanguage) return
        if (!isSource && newLangCode == sourceLanguage) return

        if (isSource) {
            sourceLanguage = newLangCode
        } else {
            targetLanguage = newLangCode
        }

        // Re-initialize translator and models for the new pair
        translator.close()
        translator = createTranslator()
        downloadTranslationModels()

        originalText = ""
        translatedText = ""
        statusMessage = "Language set to ${supportedLanguages.entries.first { it.value == newLangCode }.key}"
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
            // Check if model is already downloaded (optional optimization)
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
        speakText(originalText, isOriginal = true)
        translatedText = ""
    }

    fun stopSpeaking() {
        appTts.stop()
        isSpeaking = false
    }

    // CHANGE: Added optional parameter to select language for speech
    fun speakText(text: String, isOriginal: Boolean = false) {
        val languageToSpeak = if (isOriginal) sourceLanguage else targetLanguage
        val locale = Locale.forLanguageTag(TranslateLanguage.toLanguageTag(languageToSpeak))

        // Check if language is available for TTS
        val result = appTts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            statusMessage = "TTS language missing/not supported for: ${languageToSpeak}"
            return
        }

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

        // NOTE: Standard Android TTS produces WAV files, not MP3.
        val audioFile = File(context.getExternalFilesDir(null), "$fileName.wav")

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UTT_ID_EXPORT")

        // Use the target language for export
        val locale = Locale.forLanguageTag(TranslateLanguage.toLanguageTag(targetLanguage))
        appTts.language = locale

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
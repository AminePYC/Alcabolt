package com.example.alcabolt.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alcabolt.SpeechRecognizerHelper // <-- Added for STT functionality
import com.example.alcabolt.data.TextEntry // Assuming you have this data class
import com.example.alcabolt.data.TextEntryDao // Assuming you have this DAO interface
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
    private val dao: TextEntryDao // Database Access Object for history
) : ViewModel() {

    // --- State Management (Exposed to UI) ---
    var originalText by mutableStateOf("")
        private set
    var translatedText by mutableStateOf("")
        private set
    var isTranslating by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    var isRecording by mutableStateOf(false) // State for Audio-to-Audio input
        private set
    var statusMessage by mutableStateOf<String?>(null) // Used for Snackbar/status updates

    // --- Data Persistence ---
    val historyEntries: Flow<List<TextEntry>> = dao.getAllEntries()

    // --- Language Configuration ---
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

    var sourceLanguage by mutableStateOf(TranslateLanguage.ENGLISH)
    var targetLanguage by mutableStateOf(TranslateLanguage.FRENCH)

    // --- ML Kit & TTS Resources ---
    private var translator = createTranslator()
    private var sttHelper: SpeechRecognizerHelper? = null

    // Helper to create the ML Kit Translator client
    private fun createTranslator() = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
    )

    init {
        downloadTranslationModels()

        // Setup TTS Listener for speaking state management
        appTts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isSpeaking = true }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            override fun onError(utteranceId: String?) { isSpeaking = false; statusMessage = "TTS Error." }
        })
    }

    // --- STT/Audio-to-Audio Functions ---

    /**
     * Initializes the SpeechRecognizerHelper with all necessary callbacks.
     * Must be called from the Composable when the UI is first created.
     */
    fun initializeSttHelper(context: Context) {
        if (sttHelper == null) {
            sttHelper = SpeechRecognizerHelper(
                context = context,
                onResult = { result ->
                    // 1. Text received, set it as the original text
                    onTextChange(result)
                    // 2. STT result -> Automatic Translate & Speak (Audio-to-Audio flow)
                    translateAndSpeak()
                },
                onError = { error ->
                    statusMessage = error
                    isRecording = false
                },
                onReady = { isReady ->
                    isRecording = isReady
                    if(isReady) statusMessage = "Recording voice input..." else statusMessage = null
                }
            )
        }
    }

    /**
     * Toggles the recording state for the Audio-to-Audio Translation workflow.
     */
    fun toggleAudioToAudio() {
        if (isRecording) {
            sttHelper?.stopListening()
        } else {
            // Stop TTS if it's running before recording
            if(isSpeaking) stopSpeaking()
            // Start listening in the current source language
            sttHelper?.startListening(sourceLanguage)
        }
    }

    // --- Language and UI Handlers ---

    fun onTextChange(newText: String) {
        originalText = newText
        translatedText = ""
    }

    fun onLanguageChange(isSource: Boolean, newLangCode: String) {
        // Prevent setting the same language for source and target
        if (isSource && newLangCode == targetLanguage) return
        if (!isSource && newLangCode == sourceLanguage) return

        // Stop any active processes
        if (isRecording) toggleAudioToAudio()
        if (isSpeaking) stopSpeaking()

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

    // --- Core Translation/TTS Logic ---

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

            // Save the entry to history after successful translation and speech start
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
        translatedText = "" // Clear translated text when speaking original
    }

    fun stopSpeaking() {
        appTts.stop()
        isSpeaking = false
    }

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

    // --- Export and Database ---

    /**
     * Synthesizes text to a WAV file and saves it to external storage.
     */
    fun exportAudio(context: Context, text: String, fileName: String) {
        if (isSpeaking || isRecording) {
            statusMessage = "Please stop the current process before exporting."
            return
        }

        // Standard Android TTS produces WAV files
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

    fun deleteEntry(entry: TextEntry) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteEntry(entry.id)
        statusMessage = "Entry deleted."
    }

    // --- Resource Cleanup ---

    override fun onCleared() {
        // Essential cleanup for Android services and ML Kit client
        appTts.stop()
        appTts.shutdown()
        translator.close()
        sttHelper?.destroy()
        super.onCleared()
    }
}
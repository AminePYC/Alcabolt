package com.example.alcabolt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

/**
 * Helper class to manage the Android SpeechRecognizer service lifecycle and callbacks.
 * This keeps the TtsViewModel clean of Android-specific RecognitionListener implementation.
 */
class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onReady: (Boolean) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechRecognizerHelper)
            }
        } else {
            onError("Speech recognition is not available on this device.")
        }
    }

    /**
     * Initiates the voice listening process using the specified language code.
     */
    fun startListening(languageCode: String) {
        if (speechRecognizer == null) {
            onError("Speech recognizer not initialized.")
            return
        }

        // Convert ML Kit language code (e.g., TranslateLanguage.ENGLISH) to BCP-47 tag (e.g., en)
        val localeTag = TranslateLanguage.toLanguageTag(languageCode)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onError("Failed to start listening: ${e.message}")
            Log.e("STT", "Error starting listening", e)
        }
    }

    /**
     * Explicitly stops the listening session.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        // Note: onEndOfSpeech/onError will usually be called after this
    }

    /**
     * Releases all resources used by the SpeechRecognizer. Must be called when done.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // --- RecognitionListener Implementation ---

    override fun onReadyForSpeech(params: Bundle?) {
        onReady(true)
        Log.d("STT", "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d("STT", "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        // NOTE: We don't call onReady(false) here because onResults or onError should follow.
        Log.d("STT", "End of speech")
    }

    override fun onError(error: Int) {
        onReady(false) // Stop listening state on error
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission required: Grant microphone access."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Did not hear anything. Try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy."
            else -> "STT error: $error"
        }
        onError(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val recognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
        onReady(false) // Stop listening state on result
        recognizedText?.let {
            onResult(it) // Pass the text back to the ViewModel
        } ?: onError("Could not recognize speech.")
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
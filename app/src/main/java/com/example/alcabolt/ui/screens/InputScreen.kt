package com.example.alcabolt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.alcabolt.Screen // Assuming 'Screen' sealed class is defined elsewhere
import com.example.alcabolt.viewmodel.TtsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    navController: NavController,
    viewModel: TtsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🚨 NEW: Lifecycle management for STT helper
    LaunchedEffect(Unit) {
        viewModel.initializeSttHelper(context)

        val observer = LifecycleEventObserver { _, event ->
            // Good practice: Stop listening when the screen is paused/backgrounded
            if (event == Lifecycle.Event.ON_PAUSE && viewModel.isListening) {
                viewModel.toggleListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel.statusMessage) {
        viewModel.statusMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.statusMessage = null
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AlcaBolt Translator ⚡️", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                        Icon(Icons.Filled.History, contentDescription = "History", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 1. Language Selectors ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageDropdown(
                    label = "FROM",
                    isSource = true,
                    selectedLanguageCode = viewModel.sourceLanguage,
                    supportedLanguages = viewModel.supportedLanguages,
                    onLanguageChange = viewModel::onLanguageChange,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val tempSource = viewModel.sourceLanguage
                        viewModel.onLanguageChange(true, viewModel.targetLanguage)
                        viewModel.onLanguageChange(false, tempSource)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.CompareArrows, contentDescription = "Swap Languages", tint = MaterialTheme.colorScheme.secondary)
                }

                LanguageDropdown(
                    label = "TO",
                    isSource = false,
                    selectedLanguageCode = viewModel.targetLanguage,
                    supportedLanguages = viewModel.supportedLanguages,
                    onLanguageChange = viewModel::onLanguageChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Input/Output Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Input Area
                    TextField(
                        value = viewModel.originalText,
                        onValueChange = viewModel::onTextChange,
                        label = { Text("Enter text or speak now...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 1.dp)

                    // Output Area (Translated Text)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Translated:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (viewModel.isTranslating) "Translating..." else viewModel.translatedText.ifBlank { "Translation will appear here." },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. Control Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🚨 NEW: Speech-to-Text Toggle Button
                FloatingActionButton(
                    onClick = viewModel::toggleListening,
                    modifier = Modifier.size(64.dp),
                    containerColor = if (viewModel.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        if (viewModel.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (viewModel.isListening) "Stop Listening" else "Start Voice Input"
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Translate & Speak Button (Primary Action)
                ExtendedFloatingActionButton(
                    onClick = viewModel::translateAndSpeak,
                    modifier = Modifier.weight(1f),
                    icon = {
                        if (viewModel.isTranslating) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(Icons.Filled.Translate, contentDescription = "Translate")
                        }
                    },
                    text = { Text("Translate") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Secondary Actions (Speak Original / Export) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Speak Original Button (Replaced Stop)
                OutlinedButton(
                    onClick = viewModel::speakOriginal,
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = viewModel.originalText.isNotBlank() && !viewModel.isSpeaking,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderDefaults.outlinedButtonBorder.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Speak Original")
                    Spacer(Modifier.width(8.dp))
                    Text("Speak Input")
                }

                Spacer(Modifier.width(16.dp))

                // Export Audio Button
                OutlinedButton(
                    onClick = {
                        viewModel.exportAudio(
                            context,
                            viewModel.translatedText.ifBlank { viewModel.originalText },
                            "alcabolt_audio_${System.currentTimeMillis()}"
                        )
                    },
                    enabled = viewModel.translatedText.isNotBlank() || viewModel.originalText.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    border = BorderDefaults.outlinedButtonBorder.copy(color = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Export Audio")
                    Spacer(Modifier.width(8.dp))
                    Text("Export WAV")
                }
            }

            // 🚨 NEW: Stop Speaking Button (moved to its own row for clarity)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = viewModel::stopSpeaking,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = viewModel.isSpeaking,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderDefaults.outlinedButtonBorder.copy(color = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop Speaking")
                Spacer(Modifier.width(8.dp))
                Text("Stop TTS")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// LanguageDropdown Composable (remains the same)
@Composable
fun LanguageDropdown(
    label: String,
    isSource: Boolean,
    selectedLanguageCode: String,
    supportedLanguages: Map<String, String>,
    onLanguageChange: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLanguageName = supportedLanguages.entries
        .firstOrNull { it.value == selectedLanguageCode }?.key ?: "Select Language"

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = BorderDefaults.outlinedButtonBorder.copy(color = MaterialTheme.colorScheme.primary)
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = selectedLanguageName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            supportedLanguages.forEach { (name, code) ->
                DropdownMenuItem(
                    text = { Text(name, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onLanguageChange(isSource, code)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (code == selectedLanguageCode) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                    )
                )
            }
        }
    }
}
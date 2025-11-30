package com.example.alcabolt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // 🚨 FIX: Import all runtime composables (including remember and rememberCoroutineScope)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.alcabolt.Screen
import com.example.alcabolt.viewmodel.TtsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    navController: NavController,
    viewModel: TtsViewModel = koinViewModel()
) {
    // 🚨 FIX: Define context and scope variables here to avoid type inference issues
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show status messages as a SnackBar
    LaunchedEffect(viewModel.statusMessage) {
        viewModel.statusMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.statusMessage = null // Clear message after showing
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AlcaBolt Translator ⚡️") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Input Text Area ---
            TextField(
                value = viewModel.originalText,
                onValueChange = viewModel::onTextChange,
                label = { Text("Enter text to translate or speak") },
                placeholder = { Text("Bonjour le monde...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Translated Text Display ---
            if (viewModel.translatedText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Translated (FR):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = viewModel.translatedText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Control Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Translate & Speak Button
                FloatingActionButton(
                    onClick = viewModel::translateAndSpeak,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.isTranslating) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Filled.Translate, contentDescription = "Translate")
                            Spacer(Modifier.width(8.dp))
                            Text("Translate & Speak")
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // 2. Stop Button
                FloatingActionButton(
                    onClick = viewModel::stopSpeaking,
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Speak Original Button
            OutlinedButton(
                onClick = viewModel::speakOriginal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Speak Original")
                Spacer(Modifier.width(8.dp))
                Text("Speak Original Text")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Export Audio Button (Requires translated text to be available)
            Button(
                onClick = {
                    viewModel.exportAudio(
                        context, // Context is now correctly scoped
                        viewModel.translatedText.ifBlank { viewModel.originalText },
                        "alcabolt_audio_${System.currentTimeMillis()}"
                    )
                },
                enabled = viewModel.originalText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Filled.RecordVoiceOver, contentDescription = "Export Audio")
                Spacer(Modifier.width(8.dp))
                Text("Export Audio File")
            }
        }
    }
}
package com.example.alcabolt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.alcabolt.data.TextEntry // Ensure TextEntry is imported
import com.example.alcabolt.viewmodel.TtsViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: TtsViewModel = koinViewModel()
) {
    // 🚨 FIX: Explicitly cast the initial empty list to List<TextEntry>
    // This resolves all type inference, delegate, and 'isEmpty' ambiguity errors (lines 32, 46).
    val history: List<TextEntry> by viewModel.historyEntries.collectAsState(
        initial = emptyList<TextEntry>()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translation History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // The list type is now known, resolving 'isEmpty()' ambiguity
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No history yet. Translate something on the main screen!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // The item type is now known as TextEntry
                items(history, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onDelete = viewModel::deleteEntry,
                        onSpeak = { viewModel.speakText(entry.translatedText ?: entry.originalText) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    entry: TextEntry,
    onDelete: (TextEntry) -> Unit,
    onSpeak: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. HEADER (Timestamp and Controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Row {
                    IconButton(onClick = onSpeak) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Speak Translation",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { onDelete(entry) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 2. CONTENT (Original Text)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Original (${entry.sourceLangCode ?: "EN"}):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                // 🚨 FIX: entry.originalText is now accessible (resolves Unresolved reference 'originalText')
                Text(
                    text = entry.originalText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 3. TRANSLATED TEXT (Conditional)
            // 🚨 FIX: entry.translatedText is now accessible (resolves Unresolved reference 'translatedText')
            entry.translatedText?.let { translated ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Translation (${entry.targetLangCode ?: "FR"}):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = translated,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
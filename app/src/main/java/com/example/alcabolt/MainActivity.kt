package com.example.alcabolt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.alcabolt.data.AppDatabase
import com.example.alcabolt.ui.screens.HistoryScreen
import com.example.alcabolt.ui.screens.InputScreen
import com.example.alcabolt.ui.theme.AlcaBoltTheme
import com.example.alcabolt.viewmodel.TtsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Define Routes
sealed class Screen(val route: String) {
    data object Input : Screen("input")
    data object History : Screen("history")
}

class MainActivity : ComponentActivity() {

    private val appModule = module {
        // Single instance of TextToSpeech
        single {
            TextToSpeech(androidContext()) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    println("TTS initialization failed!")
                }
            }
        }

        // Single instance of Room Database
        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "alcabolt-db"
            ).build()
        }

        // DAO access
        single { get<AppDatabase>().textEntryDao() }

        // ViewModel instance
        viewModel { TtsViewModel(get(), get()) }
    }

    // Permission launcher for runtime permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value

            when (permission) {
                Manifest.permission.RECORD_AUDIO -> {
                    if (isGranted) {
                        println("✅ Audio recording permission granted")
                    } else {
                        println("❌ Audio recording permission denied")
                    }
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (isGranted) {
                        println("✅ Storage permission granted")
                    } else {
                        println("❌ Storage permission denied")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request necessary permissions
        requestRequiredPermissions()

        // Start Koin for dependency injection
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        setContent {
            AlcaBoltTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // NavHost with beautiful slide transitions
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Input.route,
                        enterTransition = {
                            slideInHorizontally(
                                animationSpec = tween(300)
                            ) { fullWidth -> fullWidth }
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                animationSpec = tween(300)
                            ) { fullWidth -> -fullWidth }
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                animationSpec = tween(300)
                            ) { fullWidth -> -fullWidth }
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                animationSpec = tween(300)
                            ) { fullWidth -> fullWidth }
                        }
                    ) {
                        composable(Screen.Input.route) {
                            InputScreen(navController)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(navController)
                        }
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        // Check storage permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
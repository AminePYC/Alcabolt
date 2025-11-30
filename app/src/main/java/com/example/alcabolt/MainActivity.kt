package com.example.alcabolt

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alcabolt.data.AppDatabase
import com.example.alcabolt.ui.screens.HistoryScreen
import com.example.alcabolt.ui.screens.InputScreen
import com.example.alcabolt.ui.theme.AlcaBoltTheme
import com.example.alcabolt.viewmodel.TtsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import androidx.room.Room // Import Room

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
            Room.databaseBuilder(androidContext(), AppDatabase::class.java, "alcabolt-db").build()
        }

        // DAO access
        single { get<AppDatabase>().textEntryDao() }

        // ViewModel instance
        viewModel { TtsViewModel(get(), get()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Modern full-screen look

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
                        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } },
                        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } },
                        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } },
                        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } }
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
}
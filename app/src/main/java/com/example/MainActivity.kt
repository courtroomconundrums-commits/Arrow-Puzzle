package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.sound.SoundManager
import com.example.ui.CashArrowsApp
import com.example.ui.GameViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google AdMob SDK with Test Ads
        AdMobManager.initialize(applicationContext)

        val database = GameDatabase.getInstance(applicationContext)
        val repository = GameRepository(database.gameDao())
        val soundManager = SoundManager(applicationContext)
        val factory = GameViewModel.Factory(applicationContext, repository, soundManager)

        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel(factory = factory)
                CashArrowsApp(viewModel = gameViewModel)
            }
        }
    }
}

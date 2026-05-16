package com.hanoi.binaryhanoi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hanoi.binaryhanoi.presentation.screen.HanoiApp
import com.hanoi.binaryhanoi.presentation.theme.HanoiTheme
import com.hanoi.binaryhanoi.presentation.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsState()
            HanoiTheme(mode = state.themeMode) {
                HanoiApp(state = state, viewModel = viewModel)
            }
        }
    }
}

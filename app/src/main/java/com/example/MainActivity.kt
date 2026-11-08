package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CreatorStudioApp
import com.example.ui.CreatorStudioViewModel
import com.example.ui.theme.CreatorStudioTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CreatorStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val isDarkTheme = settings.theme == "dark"

            CreatorStudioTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CreatorStudioApp(viewModel = viewModel)
                }
            }
        }
    }
}

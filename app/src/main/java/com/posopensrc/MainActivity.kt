package com.posopensrc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.navigation.AppNavGraph
import com.posopensrc.core.navigation.MainViewModel
import com.posopensrc.ui.theme.PosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val language by mainViewModel.language.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)

            PosTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        mainViewModel = mainViewModel,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }
}

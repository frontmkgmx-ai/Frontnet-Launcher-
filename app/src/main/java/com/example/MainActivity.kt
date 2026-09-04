package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.HomeScreen
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: LauncherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    applyHighRefreshRate(true)

    setContent {
      val uiState by viewModel.uiState.collectAsState()

      LaunchedEffect(uiState.highRefreshRateEnabled) {
        applyHighRefreshRate(uiState.highRefreshRateEnabled)
      }

      // Back handler to close overlay drawer or modals when user presses Back
      BackHandler(
        enabled = uiState.isDrawerOpen ||
                uiState.isCustomizeSheetOpen ||
                uiState.isAiRoutineModalOpen ||
                uiState.isSearchSheetOpen ||
                uiState.selectedAppForMenu != null
      ) {
        when {
          uiState.selectedAppForMenu != null -> viewModel.openAppMenu(null)
          uiState.isSearchSheetOpen -> viewModel.openSearchSheet(false)
          uiState.isAiRoutineModalOpen -> viewModel.openAiRoutineModal(false)
          uiState.isCustomizeSheetOpen -> viewModel.openCustomizeSheet(false)
          uiState.isDrawerOpen -> viewModel.setDrawerOpen(false)
        }
      }

      MyApplicationTheme(wallpaperTheme = uiState.currentWallpaper) {
        Surface(modifier = Modifier.fillMaxSize()) {
          HomeScreen(viewModel = viewModel)
        }
      }
    }
  }

  private fun applyHighRefreshRate(enabled: Boolean) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowParams = window.attributes
        if (enabled) {
          val currentDisplay = display
          val modes = currentDisplay?.supportedModes ?: emptyArray()
          val bestMode = modes.filter { it.refreshRate >= 60f }.maxByOrNull { it.refreshRate }
          if (bestMode != null) {
            windowParams.preferredDisplayModeId = bestMode.modeId
          }
          @Suppress("DEPRECATION")
          windowParams.preferredRefreshRate = bestMode?.refreshRate ?: 120f
        } else {
          windowParams.preferredDisplayModeId = 0
          @Suppress("DEPRECATION")
          windowParams.preferredRefreshRate = 0f
        }
        window.attributes = windowParams
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val windowParams = window.attributes
        @Suppress("DEPRECATION")
        windowParams.preferredRefreshRate = if (enabled) 120f else 0f
        window.attributes = windowParams
      }
    } catch (e: Exception) {
      // Ignored if device vendor restricts manual display mode override
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshApps()
  }
}


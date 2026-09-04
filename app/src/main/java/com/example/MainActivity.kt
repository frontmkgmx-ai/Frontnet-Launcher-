package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
    setContent {
      val uiState by viewModel.uiState.collectAsState()

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

  override fun onResume() {
    super.onResume()
    viewModel.refreshApps()
  }
}


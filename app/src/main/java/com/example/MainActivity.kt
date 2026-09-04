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

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.example.widget.WidgetHostManager

class MainActivity : ComponentActivity() {

  private val viewModel: LauncherViewModel by viewModels()
  lateinit var widgetHostManager: WidgetHostManager

  private val pickWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (appWidgetId != -1) {
            val appWidgetInfo = widgetHostManager.appWidgetManager.getAppWidgetInfo(appWidgetId)
            if (appWidgetInfo.configure != null) {
                // Needs configuration
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                intent.component = appWidgetInfo.configure
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                configureWidgetLauncher.launch(intent)
            } else {
                // Ready to add
                viewModel.addWidget(appWidgetId)
            }
        }
    } else {
        // Cancelled, delete the allocated ID
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (appWidgetId != -1) {
            widgetHostManager.deleteAppWidgetId(appWidgetId)
        }
    }
  }

  private val configureWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
      if (result.resultCode == Activity.RESULT_OK && appWidgetId != -1) {
          viewModel.addWidget(appWidgetId)
      } else if (appWidgetId != -1) {
          widgetHostManager.deleteAppWidgetId(appWidgetId)
      }
  }

  fun requestDefaultLauncher() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val roleManager = getSystemService(android.app.role.RoleManager::class.java)
          if (roleManager?.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) == true &&
              !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
          ) {
              val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
              startActivityForResult(intent, 1001)
          }
      } else {
          try {
              val intent = Intent(Settings.ACTION_HOME_SETTINGS)
              startActivity(intent)
          } catch (e: Exception) {
              val intent = Intent(Settings.ACTION_SETTINGS)
              startActivity(intent)
          }
      }
  }

  fun launchWidgetPicker() {
      val appWidgetId = widgetHostManager.allocateAppWidgetId()
      val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
      pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      pickWidgetLauncher.launch(pickIntent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    applyHighRefreshRate(true)

    widgetHostManager = WidgetHostManager(this)

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

  override fun onStart() {
    super.onStart()
    widgetHostManager.startListening()
  }

  override fun onStop() {
    super.onStop()
    widgetHostManager.stopListening()
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshApps()
  }
}


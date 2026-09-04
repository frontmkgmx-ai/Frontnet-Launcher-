package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiLauncherService
import com.example.data.AppDatabase
import com.example.data.LauncherConfigEntity
import com.example.data.LauncherRepository
import com.example.model.AiDailyBriefing
import com.example.model.AppCategory
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle
import com.example.ui.theme.WallpaperTheme
import com.example.ui.theme.WallpaperThemeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<LauncherApp> = emptyList(),
    val dockApps: List<LauncherApp> = emptyList(),
    val topUsedApps: List<LauncherApp> = emptyList(),
    val searchQuery: String = "",
    val isDrawerOpen: Boolean = false,
    val drawerCategorized: Boolean = true,
    val selectedCategoryFilter: AppCategory? = null,
    val isCustomizeSheetOpen: Boolean = false,
    val isAiRoutineModalOpen: Boolean = false,
    val isSearchSheetOpen: Boolean = false,
    val selectedAppForMenu: LauncherApp? = null,
    val isChangeCategoryDialogOpen: Boolean = false,
    val themeStyle: LauncherThemeStyle = LauncherThemeStyle.MATERIAL_YOU,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconThemed: Boolean = false,
    val iconSizeDp: Int = 56,
    val showLabels: Boolean = true,
    val currentWallpaper: WallpaperTheme = WallpaperThemeEngine.wallpaperList.first(),
    val aiBriefing: AiDailyBriefing? = null,
    val isAiLoading: Boolean = false,
    val gestureSwipeDown: GestureAction = GestureAction.SEARCH,
    val gestureDoubleTap: GestureAction = GestureAction.AI_ROUTINE,
    val statusMessage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(
        application.applicationContext,
        AppDatabase.getInstance(application.applicationContext)
    )

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        loadConfigAndApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            val allApps = repository.getInstalledOrPreloadedApps()
            val dock = allApps.filter { it.isPinnedToDock }.take(5)
            val topUsed = allApps.sortedByDescending { it.launchCount }.take(6)

            _uiState.update { current ->
                current.copy(
                    apps = allApps,
                    dockApps = dock,
                    topUsedApps = topUsed
                )
            }

            refreshAiSuggestions()
        }
    }

    private fun loadConfigAndApps() {
        viewModelScope.launch {
            repository.configFlow.collect { config ->
                val cfg = config ?: LauncherConfigEntity()
                val theme = try {
                    LauncherThemeStyle.valueOf(cfg.themeStyle)
                } catch (e: Exception) {
                    LauncherThemeStyle.MATERIAL_YOU
                }
                val shape = try {
                    IconShape.valueOf(cfg.iconShape)
                } catch (e: Exception) {
                    IconShape.SQUIRCLE
                }
                val wallpaper = WallpaperThemeEngine.getThemeById(cfg.wallpaperId)
                val swipeDownAct = try {
                    GestureAction.valueOf(cfg.gestureSwipeDownAction)
                } catch (e: Exception) {
                    GestureAction.SEARCH
                }
                val doubleTapAct = try {
                    GestureAction.valueOf(cfg.gestureDoubleTapAction)
                } catch (e: Exception) {
                    GestureAction.AI_ROUTINE
                }

                _uiState.update { current ->
                    current.copy(
                        themeStyle = theme,
                        iconShape = shape,
                        iconThemed = cfg.iconThemed,
                        iconSizeDp = cfg.iconSizeDp,
                        showLabels = cfg.showLabels,
                        currentWallpaper = wallpaper,
                        drawerCategorized = cfg.drawerCategorized,
                        gestureSwipeDown = swipeDownAct,
                        gestureDoubleTap = doubleTapAct
                    )
                }
            }
        }

        refreshApps()
    }

    fun refreshAiSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val briefing = GeminiLauncherService.getDailyRoutineSuggestions(
                _uiState.value.apps,
                _uiState.value.topUsedApps
            )
            _uiState.update { it.copy(aiBriefing = briefing, isAiLoading = false) }
        }
    }

    fun launchApp(app: LauncherApp) {
        viewModelScope.launch {
            repository.launchApp(app.packageName)
            _uiState.update { it.copy(isDrawerOpen = false, isSearchSheetOpen = false) }
            // Increment local state launch count
            _uiState.update { current ->
                val updated = current.apps.map {
                    if (it.packageName == app.packageName) it.copy(launchCount = it.launchCount + 1) else it
                }
                current.copy(
                    apps = updated,
                    topUsedApps = updated.sortedByDescending { it.launchCount }.take(6)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setDrawerOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isDrawerOpen = isOpen, searchQuery = if (!isOpen) "" else it.searchQuery) }
    }

    fun toggleDrawerCategorized() {
        val newMode = !_uiState.value.drawerCategorized
        _uiState.update { it.copy(drawerCategorized = newMode) }
        persistConfig()
    }

    fun selectCategoryFilter(category: AppCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun openCustomizeSheet(open: Boolean) {
        _uiState.update { it.copy(isCustomizeSheetOpen = open) }
    }

    fun openAiRoutineModal(open: Boolean) {
        _uiState.update { it.copy(isAiRoutineModalOpen = open) }
    }

    fun openSearchSheet(open: Boolean) {
        _uiState.update { it.copy(isSearchSheetOpen = open) }
    }

    fun openAppMenu(app: LauncherApp?) {
        _uiState.update { it.copy(selectedAppForMenu = app) }
    }

    fun openChangeCategoryDialog(open: Boolean) {
        _uiState.update { it.copy(isChangeCategoryDialogOpen = open) }
    }

    fun togglePinAppToDock(app: LauncherApp) {
        viewModelScope.launch {
            repository.togglePinToDock(app.packageName, app.isPinnedToDock)
            val updatedApps = _uiState.value.apps.map {
                if (it.packageName == app.packageName) it.copy(isPinnedToDock = !it.isPinnedToDock) else it
            }
            _uiState.update {
                it.copy(
                    apps = updatedApps,
                    dockApps = updatedApps.filter { a -> a.isPinnedToDock }.take(5),
                    selectedAppForMenu = null
                )
            }
        }
    }

    fun changeAppCategory(app: LauncherApp, newCategory: AppCategory) {
        viewModelScope.launch {
            repository.updateCategory(app.packageName, newCategory)
            val updatedApps = _uiState.value.apps.map {
                if (it.packageName == app.packageName) it.copy(category = newCategory) else it
            }
            _uiState.update {
                it.copy(
                    apps = updatedApps,
                    selectedAppForMenu = null,
                    isChangeCategoryDialogOpen = false
                )
            }
        }
    }

    fun setLauncherThemeStyle(themeStyle: LauncherThemeStyle) {
        _uiState.update { it.copy(themeStyle = themeStyle) }
        persistConfig()
    }

    fun setIconShape(shape: IconShape) {
        _uiState.update { it.copy(iconShape = shape) }
        persistConfig()
    }

    fun setIconThemed(themed: Boolean) {
        _uiState.update { it.copy(iconThemed = themed) }
        persistConfig()
    }

    fun setIconSize(sizeDp: Int) {
        _uiState.update { it.copy(iconSizeDp = sizeDp) }
        persistConfig()
    }

    fun setShowLabels(show: Boolean) {
        _uiState.update { it.copy(showLabels = show) }
        persistConfig()
    }

    fun setWallpaper(wallpaper: WallpaperTheme) {
        _uiState.update { it.copy(currentWallpaper = wallpaper) }
        persistConfig()
    }

    fun setGestureSwipeDown(action: GestureAction) {
        _uiState.update { it.copy(gestureSwipeDown = action) }
        persistConfig()
    }

    fun setGestureDoubleTap(action: GestureAction) {
        _uiState.update { it.copy(gestureDoubleTap = action) }
        persistConfig()
    }

    fun handleSwipeDown() {
        when (_uiState.value.gestureSwipeDown) {
            GestureAction.SEARCH -> openSearchSheet(true)
            GestureAction.APP_DRAWER -> setDrawerOpen(true)
            GestureAction.AI_ROUTINE -> openAiRoutineModal(true)
            GestureAction.CUSTOMIZE -> openCustomizeSheet(true)
            GestureAction.SETTINGS -> openCustomizeSheet(true)
            GestureAction.NOTIFICATIONS -> {
                _uiState.update { it.copy(statusMessage = "Barra de notificações expandida") }
            }
        }
    }

    fun handleDoubleTap() {
        when (_uiState.value.gestureDoubleTap) {
            GestureAction.AI_ROUTINE -> openAiRoutineModal(true)
            GestureAction.APP_DRAWER -> setDrawerOpen(true)
            GestureAction.SEARCH -> openSearchSheet(true)
            GestureAction.CUSTOMIZE -> openCustomizeSheet(true)
            GestureAction.SETTINGS -> openCustomizeSheet(true)
            GestureAction.NOTIFICATIONS -> {
                _uiState.update { it.copy(statusMessage = "Toque duplo executado") }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun persistConfig() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.saveConfig(
                LauncherConfigEntity(
                    id = 1,
                    themeStyle = state.themeStyle.name,
                    iconShape = state.iconShape.name,
                    iconThemed = state.iconThemed,
                    iconSizeDp = state.iconSizeDp,
                    showLabels = state.showLabels,
                    wallpaperId = state.currentWallpaper.id,
                    drawerCategorized = state.drawerCategorized,
                    gestureSwipeDownAction = state.gestureSwipeDown.name,
                    gestureDoubleTapAction = state.gestureDoubleTap.name
                )
            )
        }
    }
}

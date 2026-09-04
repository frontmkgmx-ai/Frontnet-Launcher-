package com.example.ui

import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.Icons
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.FrontAiService
import com.example.data.AppDatabase
import com.example.data.LauncherConfigEntity
import com.example.data.LauncherRepository
import com.example.model.AiDailyBriefing
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.LauncherApp
import com.example.model.LauncherThemeStyle
import com.example.ui.theme.WallpaperTheme
import com.example.ui.theme.WallpaperThemeEngine
import com.example.util.DefaultAppsResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<LauncherApp> = emptyList(),
    val appItems: List<AppItem> = emptyList(),
    val dockApps: List<LauncherApp> = emptyList(),
    val dockAppItems: List<AppItem> = emptyList(),
    val topUsedApps: List<LauncherApp> = emptyList(),
    val searchQuery: String = "",
    val isDrawerOpen: Boolean = false,
    val drawerCategorized: Boolean = true,
    val addNewAppsToHome: Boolean = true,
    val hasInitializedDock: Boolean = false,
    val selectedCategoryFilter: AppCategory? = null,
    val isCustomizeSheetOpen: Boolean = false,
    val isAiRoutineModalOpen: Boolean = false,
    val isSearchSheetOpen: Boolean = false,
    val selectedAppForMenu: LauncherApp? = null,
    val isChangeCategoryDialogOpen: Boolean = false,
    val isFirstRunCompleted: Boolean = true, // Defaults to true until config is loaded
    val isWelcomeOnboardingOpen: Boolean = false,
    val highRefreshRateEnabled: Boolean = true,
    val themeStyle: LauncherThemeStyle = LauncherThemeStyle.MATERIAL_YOU,
    val homeScreenStyle: com.example.model.HomeScreenStyle = com.example.model.HomeScreenStyle.CLASSIC_GRID,
    val appDrawerStyle: com.example.model.AppDrawerStyle = com.example.model.AppDrawerStyle.CATEGORY_TABS,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconThemed: Boolean = false,
    val iconSizeDp: Int = 56,
    val showLabels: Boolean = true,
    val widgets: List<Int> = emptyList(),
    val currentWallpaper: WallpaperTheme = WallpaperThemeEngine.wallpaperList.first(),
    val aiBriefing: AiDailyBriefing? = null,
    val isAiLoading: Boolean = false,
    val gestureSwipeDown: GestureAction = GestureAction.SEARCH,
    val gestureDoubleTap: GestureAction = GestureAction.AI_ROUTINE,
    val statusMessage: String? = null,
    val isDockConfigOpen: Boolean = false,
    val isAppLockConfigOpen: Boolean = false,
    val isSettingsPageOpen: Boolean = false,
    val isAiEnabled: Boolean = true,
    val customWallpaperUri: String? = null,
    val homePageCount: Int = 1,
    val isEditMode: Boolean = false,
    val gridColumns: Int = 4,
    val renderingMode: String = "GPU_REAL"
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
            val settingsApp = LauncherApp(
                packageName = "com.front.launcher.settings",
                label = "Configurações do Launcher",
                category = com.example.model.AppCategory.TOOLS,
                isLauncherSettingsShortcut = true,
                iconVector = Icons.Rounded.Settings,
                iconTint = androidx.compose.ui.graphics.Color(0xFF64748B)
            )
            val allApps = repository.getInstalledOrPreloadedApps() + settingsApp
            
            // Resolve dock apps: if none pinned, detect default essential apps (Phone, Messages, Browser, Camera)
            val dock = if (allApps.any { it.isPinnedToDock }) {
                allApps.filter { it.isPinnedToDock }.take(5)
            } else {
                val resolvedDefaults = DefaultAppsResolver.resolveDefaultLauncherApps(getApplication(), allApps)
                // Persist the default pinned apps in background
                for (app in resolvedDefaults) {
                    repository.togglePinToDock(app.packageName, false)
                }
                resolvedDefaults
            }

            val topUsed = allApps.sortedByDescending { it.launchCount }.take(6)

            // Convert pre-decoded icons to AppItem models
            val appItems = allApps.mapNotNull { app ->
                val iconBitmap = app.icon ?: com.example.util.AppIconCache.getCachedImageBitmap(app.packageName)
                if (iconBitmap != null) {
                    AppItem(label = app.label, packageName = app.packageName, icon = iconBitmap)
                } else null
            }
            val dockItems = dock.mapNotNull { app ->
                val iconBitmap = app.icon ?: com.example.util.AppIconCache.getCachedImageBitmap(app.packageName)
                if (iconBitmap != null) {
                    AppItem(label = app.label, packageName = app.packageName, icon = iconBitmap)
                } else null
            }

            _uiState.update { current ->
                current.copy(
                    apps = allApps,
                    appItems = appItems,
                    dockApps = dock,
                    dockAppItems = dockItems,
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
                val homeStyle = try {
                    com.example.model.HomeScreenStyle.valueOf(cfg.homeScreenStyle)
                } catch (e: Exception) {
                    com.example.model.HomeScreenStyle.CLASSIC_GRID
                }
                val drawerStyle = try {
                    com.example.model.AppDrawerStyle.valueOf(cfg.appDrawerStyle)
                } catch (e: Exception) {
                    com.example.model.AppDrawerStyle.CATEGORY_TABS
                }
                
                val widgetsList = try {
                    if (cfg.widgetsJson.isBlank() || cfg.widgetsJson == "[]") {
                        emptyList<Int>()
                    } else {
                        cfg.widgetsJson.removeSurrounding("[", "]").split(",")
                            .mapNotNull { it.trim().toIntOrNull() }
                    }
                } catch (e: Exception) {
                    emptyList<Int>()
                }

                _uiState.update { current ->
                    current.copy(
                        themeStyle = theme,
                        homeScreenStyle = homeStyle,
                        appDrawerStyle = drawerStyle,
                        widgets = widgetsList,
                        iconShape = shape,
                        iconThemed = cfg.iconThemed,
                        iconSizeDp = cfg.iconSizeDp,
                        showLabels = cfg.showLabels,
                        currentWallpaper = wallpaper,
                        drawerCategorized = cfg.drawerCategorized,
                        addNewAppsToHome = cfg.addNewAppsToHome,
                        hasInitializedDock = cfg.hasInitializedDock,
                        gestureSwipeDown = swipeDownAct,
                        gestureDoubleTap = doubleTapAct,
                        isFirstRunCompleted = cfg.isFirstRunCompleted,
                        isWelcomeOnboardingOpen = !cfg.isFirstRunCompleted,
                        highRefreshRateEnabled = cfg.highRefreshRateEnabled,
                        isAiEnabled = cfg.isAiEnabled,
                        customWallpaperUri = cfg.customWallpaperUri,
                        homePageCount = cfg.homePageCount,
                        renderingMode = cfg.renderingMode
                    )
                }
            }
        }

        refreshApps()
    }

    fun refreshAiSuggestions() {
        if (!_uiState.value.isAiEnabled) {
            _uiState.update { it.copy(aiBriefing = null, isAiLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val briefing = FrontAiService.getDailyRoutineSuggestions(
                _uiState.value.apps,
                _uiState.value.topUsedApps
            )
            _uiState.update { it.copy(aiBriefing = briefing, isAiLoading = false) }
        }
    }

    fun launchApp(app: LauncherApp) {
        if (app.isLauncherSettingsShortcut || app.packageName == "com.front.launcher.settings") {
            openSettingsPage(true)
            _uiState.update { it.copy(isDrawerOpen = false, isSearchSheetOpen = false) }
            return
        }

        viewModelScope.launch {
            repository.launchApp(app.packageName)
            _uiState.update { it.copy(isDrawerOpen = false, isSearchSheetOpen = false) }
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

    fun completeFirstRun() {
        viewModelScope.launch {
            repository.setFirstRunCompleted(true)
            _uiState.update { it.copy(isFirstRunCompleted = true, isWelcomeOnboardingOpen = false) }
        }
    }

    fun setWelcomeOnboardingOpen(open: Boolean) {
        _uiState.update { it.copy(isWelcomeOnboardingOpen = open) }
    }

    fun setHighRefreshRateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHighRefreshRateEnabled(enabled)
            _uiState.update { it.copy(highRefreshRateEnabled = enabled) }
        }
    }

    fun addWidget(appWidgetId: Int) {
        _uiState.update { current ->
            val updated = current.widgets.toMutableList().apply {
                if (!contains(appWidgetId)) add(appWidgetId)
            }
            current.copy(widgets = updated)
        }
        persistConfig()
    }

    fun removeWidget(appWidgetId: Int) {
        _uiState.update { current ->
            current.copy(widgets = current.widgets.filter { it != appWidgetId })
        }
        persistConfig()
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

    fun setDockConfigOpen(open: Boolean) {
        _uiState.update { it.copy(isDockConfigOpen = open) }
    }

    fun setAppLockConfigOpen(open: Boolean) {
        _uiState.update { it.copy(isAppLockConfigOpen = open) }
    }

    fun toggleAppLock(app: LauncherApp) {
        viewModelScope.launch {
            repository.toggleAppLock(app.packageName, app.isLocked)
            val updatedApps = _uiState.value.apps.map {
                if (it.packageName == app.packageName) it.copy(isLocked = !it.isLocked) else it
            }
            _uiState.update { it.copy(apps = updatedApps) }
        }
    }

    fun toggleAppHidden(app: LauncherApp) {
        viewModelScope.launch {
            repository.toggleAppHidden(app.packageName, app.isHidden)
            val updatedApps = _uiState.value.apps.map {
                if (it.packageName == app.packageName) it.copy(isHidden = !it.isHidden) else it
            }
            _uiState.update { it.copy(apps = updatedApps) }
        }
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

    fun setHomeScreenStyle(style: com.example.model.HomeScreenStyle) {
        _uiState.update { it.copy(homeScreenStyle = style) }
        persistConfig()
    }

    fun setAppDrawerStyle(style: com.example.model.AppDrawerStyle) {
        _uiState.update { it.copy(appDrawerStyle = style) }
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
                    homeScreenStyle = state.homeScreenStyle.name,
                    appDrawerStyle = state.appDrawerStyle.name,
                    widgetsJson = "[${state.widgets.joinToString(",")}]",
                    iconShape = state.iconShape.name,
                    iconThemed = state.iconThemed,
                    iconSizeDp = state.iconSizeDp,
                    showLabels = state.showLabels,
                    wallpaperId = state.currentWallpaper.id,
                    drawerCategorized = state.drawerCategorized,
                    addNewAppsToHome = state.addNewAppsToHome,
                    dockApps = state.dockApps.map { it.packageName },
                    hasInitializedDock = state.hasInitializedDock,
                    gestureSwipeDownAction = state.gestureSwipeDown.name,
                    gestureDoubleTapAction = state.gestureDoubleTap.name,
                    isFirstRunCompleted = state.isFirstRunCompleted,
                    highRefreshRateEnabled = state.highRefreshRateEnabled,
                    isAiEnabled = state.isAiEnabled,
                    customWallpaperUri = state.customWallpaperUri,
                    homePageCount = state.homePageCount,
                    renderingMode = state.renderingMode
                )
            )
        }
    }

    fun setRenderingMode(mode: String) {
        _uiState.update { it.copy(renderingMode = mode) }
        persistConfig()
    }

    fun toggleAddNewAppsToHome() {
        val newMode = !_uiState.value.addNewAppsToHome
        _uiState.update { it.copy(addNewAppsToHome = newMode) }
        persistConfig()
    }

    fun openSettingsPage(open: Boolean) {
        _uiState.update { it.copy(isSettingsPageOpen = open, isCustomizeSheetOpen = false) }
    }

    fun setAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAiEnabled = enabled) }
        if (!enabled) {
            _uiState.update { it.copy(aiBriefing = null) }
        } else {
            refreshAiSuggestions()
        }
        persistConfig()
    }

    fun setCustomWallpaperUri(uri: String?) {
        _uiState.update { it.copy(customWallpaperUri = uri) }
        persistConfig()
    }

    fun addHomePage() {
        _uiState.update { it.copy(homePageCount = it.homePageCount + 1) }
        persistConfig()
    }

    fun removeHomePage() {
        if (_uiState.value.homePageCount > 1) {
            _uiState.update { it.copy(homePageCount = it.homePageCount - 1) }
            persistConfig()
        }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    fun setGridColumns(cols: Int) {
        _uiState.update { it.copy(gridColumns = cols.coerceIn(3, 6)) }
        persistConfig()
    }

    fun setDrawerCategorized(categorized: Boolean) {
        _uiState.update { it.copy(drawerCategorized = categorized) }
        persistConfig()
    }

    fun updateDockApps(newDockApps: List<LauncherApp>) {
        val limited = newDockApps.take(5)
        viewModelScope.launch {
            // Unpin all current apps
            for (app in _uiState.value.apps) {
                if (app.isPinnedToDock && limited.none { it.packageName == app.packageName }) {
                    repository.togglePinToDock(app.packageName, true)
                }
            }
            // Pin selected apps
            for (app in limited) {
                repository.togglePinToDock(app.packageName, false)
            }
            val updatedApps = _uiState.value.apps.map { current ->
                current.copy(isPinnedToDock = limited.any { it.packageName == current.packageName })
            }
            _uiState.update {
                it.copy(
                    apps = updatedApps,
                    dockApps = limited.map { a -> a.copy(isPinnedToDock = true) },
                    hasInitializedDock = true
                )
            }
            persistConfig()
        }
    }
}



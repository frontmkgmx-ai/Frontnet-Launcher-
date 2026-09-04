package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherApp
import com.example.ui.components.AiDailySuggestionsSection
import com.example.ui.components.AiRoutineModal
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppIconComposable
import com.example.ui.components.CategorySelectionDialog
import com.example.ui.components.CustomizeLauncherSheet
import com.example.ui.components.DockBar
import com.example.ui.components.HomeScreenHeader
import com.example.ui.components.SearchSheet
import com.example.ui.components.WelcomeOnboardingScreen

import com.example.MainActivity
import com.example.widget.AppWidgetComposable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? MainActivity

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    var verticalDragAmount by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            // Gesture detection for wallpaper canvas
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.handleDoubleTap()
                    },
                    onLongPress = {
                        viewModel.openCustomizeSheet(true)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDragAmount = 0f },
                    onDragEnd = {
                        if (verticalDragAmount < -40f) {
                            // Swipe UP -> open App Drawer
                            viewModel.setDrawerOpen(true)
                        } else if (verticalDragAmount > 40f) {
                            // Swipe DOWN -> configured action (Search, AI Routine, etc.)
                            viewModel.handleSwipeDown()
                        }
                        verticalDragAmount = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        verticalDragAmount += dragAmount
                    }
                )
            }
            .testTag("home_screen_canvas")
    ) {
        // Main Home Screen Layout
        androidx.compose.animation.AnimatedVisibility(
            visible = !uiState.isDrawerOpen && !uiState.isCustomizeSheetOpen,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Adaptive Header (Material You / One UI / HyperOS)
                HomeScreenHeader(
                themeStyle = uiState.themeStyle,
                onSearchClick = { viewModel.openSearchSheet(true) },
                onAiRoutineClick = { viewModel.openAiRoutineModal(true) },
                onCustomizeClick = { viewModel.openCustomizeSheet(true) }
            )

            // AI Daily Routine & Suggestions Card
            AiDailySuggestionsSection(
                briefing = uiState.aiBriefing,
                installedApps = uiState.apps,
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                themeStyle = uiState.themeStyle,
                isLoading = uiState.isAiLoading,
                onRefreshClick = { viewModel.refreshAiSuggestions() },
                onAppClick = { viewModel.launchApp(it) },
                onOpenBriefingModal = { viewModel.openAiRoutineModal(true) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Widgets
            if (uiState.widgets.isNotEmpty() && activity != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.widgets.forEach { appWidgetId ->
                        val providerInfo = remember(appWidgetId) {
                            activity.widgetHostManager.appWidgetManager.getAppWidgetInfo(appWidgetId)
                        }
                        if (providerInfo != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                viewModel.removeWidget(appWidgetId)
                                                activity.widgetHostManager.deleteAppWidgetId(appWidgetId)
                                            }
                                        )
                                    }
                            ) {
                                AppWidgetComposable(
                                    widgetHostManager = activity.widgetHostManager,
                                    appWidgetId = appWidgetId,
                                    providerInfo = providerInfo
                                )
                            }
                        }
                    }
                }
            }

            // Primary Home Apps Grid (Top Favorites / Frequently Used + Front Settings Shortcut)
            val homeApps = remember(uiState.apps, uiState.dockApps) {
                val nonDockApps = uiState.apps.filter { !it.isPinnedToDock }
                val settingsShortcut = nonDockApps.firstOrNull { it.isLauncherSettingsShortcut }
                val regularApps = nonDockApps.filter { !it.isLauncherSettingsShortcut }
                if (settingsShortcut != null) {
                    listOf(settingsShortcut) + regularApps.take(7)
                } else {
                    nonDockApps.take(8)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                when (uiState.homeScreenStyle) {
                    com.example.model.HomeScreenStyle.CLASSIC_GRID -> {
                        val rows = homeApps.chunked(4)
                        for (row in rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (app in row) {
                                    AppIconComposable(
                                        app = app,
                                        iconShape = uiState.iconShape,
                                        iconSizeDp = uiState.iconSizeDp,
                                        iconThemed = uiState.iconThemed,
                                        showLabel = uiState.showLabels,
                                        onClick = { viewModel.launchApp(app) },
                                        onLongClick = { viewModel.openAppMenu(app) }
                                    )
                                }
                                for (i in row.size until 4) {
                                    Spacer(modifier = Modifier.fillMaxWidth(0.25f))
                                }
                            }
                        }
                    }
                    com.example.model.HomeScreenStyle.MINIMALIST_TEXT -> {
                        for (app in homeApps) {
                            Text(
                                text = app.label.lowercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { viewModel.launchApp(app) }
                            )
                        }
                    }
                    com.example.model.HomeScreenStyle.DOCK_ONLY -> {
                        // Empty middle area, everything is in the dock or widgets
                    }
                }
            }

            // Bottom Navigation Dock
            DockBar(
                dockApps = uiState.dockApps,
                iconShape = uiState.iconShape,
                iconSizeDp = uiState.iconSizeDp,
                iconThemed = uiState.iconThemed,
                themeStyle = uiState.themeStyle,
                dockBlurColor = uiState.currentWallpaper.dockBlurColor,
                onAppClick = { viewModel.launchApp(it) },
                onAppLongClick = { viewModel.openAppMenu(it) },
                onOpenDrawer = { viewModel.setDrawerOpen(true) }
            )
        }
        } // Close AnimatedVisibility

        // Animated Overlays:
        // 1. App Drawer Sheet (Auto-Categorization mode & A-Z mode)
        AnimatedVisibility(
            visible = uiState.isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerSheet(
                isOpen = uiState.isDrawerOpen,
                apps = uiState.apps,
                searchQuery = uiState.searchQuery,
                drawerStyle = uiState.appDrawerStyle,
                selectedCategoryFilter = uiState.selectedCategoryFilter,
                iconShape = uiState.iconShape,
                iconSizeDp = uiState.iconSizeDp,
                iconThemed = uiState.iconThemed,
                showLabels = uiState.showLabels,
                themeStyle = uiState.themeStyle,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSelectCategoryFilter = { viewModel.selectCategoryFilter(it) },
                onAppClick = { viewModel.launchApp(it) },
                onAppLongClick = { viewModel.openAppMenu(it) },
                onCloseDrawer = { viewModel.setDrawerOpen(false) }
            )
        }

        // 2. Customize Launcher Sheet
        AnimatedVisibility(
            visible = uiState.isCustomizeSheetOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            CustomizeLauncherSheet(
                isOpen = uiState.isCustomizeSheetOpen,
                themeStyle = uiState.themeStyle,
                homeScreenStyle = uiState.homeScreenStyle,
                appDrawerStyle = uiState.appDrawerStyle,
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                iconSizeDp = uiState.iconSizeDp,
                showLabels = uiState.showLabels,
                currentWallpaper = uiState.currentWallpaper,
                gestureSwipeDown = uiState.gestureSwipeDown,
                gestureDoubleTap = uiState.gestureDoubleTap,
                highRefreshRateEnabled = uiState.highRefreshRateEnabled,
                onThemeStyleChange = { viewModel.setLauncherThemeStyle(it) },
                onHomeScreenStyleChange = { viewModel.setHomeScreenStyle(it) },
                onAppDrawerStyleChange = { viewModel.setAppDrawerStyle(it) },
                onIconShapeChange = { viewModel.setIconShape(it) },
                onIconThemedChange = { viewModel.setIconThemed(it) },
                onIconSizeChange = { viewModel.setIconSize(it) },
                onShowLabelsChange = { viewModel.setShowLabels(it) },
                onWallpaperChange = { viewModel.setWallpaper(it) },
                onGestureSwipeDownChange = { viewModel.setGestureSwipeDown(it) },
                onGestureDoubleTapChange = { viewModel.setGestureDoubleTap(it) },
                onHighRefreshRateChange = { viewModel.setHighRefreshRateEnabled(it) },
                onOpenWelcomeOnboarding = { viewModel.setWelcomeOnboardingOpen(true) },
                onSetDefaultLauncherClick = { activity?.requestDefaultLauncher() },
                onAddWidgetClick = {
                    activity?.launchWidgetPicker()
                    viewModel.openCustomizeSheet(false)
                },
                onClose = { viewModel.openCustomizeSheet(false) }
            )
        }

        // 3. AI Daily Routine Modal
        AnimatedVisibility(
            visible = uiState.isAiRoutineModalOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AiRoutineModal(
                isOpen = uiState.isAiRoutineModalOpen,
                briefing = uiState.aiBriefing,
                isLoading = uiState.isAiLoading,
                installedApps = uiState.apps,
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                onRefresh = { viewModel.refreshAiSuggestions() },
                onAppClick = { viewModel.launchApp(it) },
                onClose = { viewModel.openAiRoutineModal(false) }
            )
        }

        // 4. Universal Quick Search Sheet
        AnimatedVisibility(
            visible = uiState.isSearchSheetOpen,
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
        ) {
            SearchSheet(
                isOpen = uiState.isSearchSheetOpen,
                apps = uiState.apps,
                searchQuery = uiState.searchQuery,
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onAppClick = { viewModel.launchApp(it) },
                onClose = { viewModel.openSearchSheet(false) }
            )
        }

        // 5. App Long-press Menu Dialog
        AppActionDialog(
            app = uiState.selectedAppForMenu,
            iconShape = uiState.iconShape,
            iconThemed = uiState.iconThemed,
            onDismiss = { viewModel.openAppMenu(null) },
            onLaunch = { viewModel.launchApp(it) },
            onTogglePinDock = { viewModel.togglePinAppToDock(it) },
            onChangeCategoryClick = { viewModel.openChangeCategoryDialog(true) }
        )

        // 6. Category Selection Dialog
        CategorySelectionDialog(
            isOpen = uiState.isChangeCategoryDialogOpen,
            app = uiState.selectedAppForMenu,
            onDismiss = { viewModel.openChangeCategoryDialog(false) },
            onCategorySelected = { newCat ->
                uiState.selectedAppForMenu?.let { app ->
                    viewModel.changeAppCategory(app, newCat)
                }
            }
        )

        // 7. Welcome Onboarding Dialog / First Run Setup
        WelcomeOnboardingScreen(
            isOpen = uiState.isWelcomeOnboardingOpen,
            currentThemeStyle = uiState.themeStyle,
            onSelectThemeStyle = { viewModel.setLauncherThemeStyle(it) },
            onComplete = { viewModel.completeFirstRun() }
        )

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

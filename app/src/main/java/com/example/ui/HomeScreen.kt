package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainActivity
import com.example.model.LauncherApp
import com.example.ui.components.AiDailySuggestionsSection
import com.example.ui.components.AiRoutineModal
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppIconComposable
import com.example.ui.components.AppLockConfigDialog
import com.example.ui.components.CategorySelectionDialog
import com.example.ui.components.CustomizeLauncherSheet
import com.example.ui.components.DockBar
import com.example.ui.components.DockConfigDialog
import com.example.ui.components.HomeScreenHeader
import com.example.ui.components.LauncherSettingsScreen
import com.example.ui.components.SearchSheet
import com.example.ui.components.WelcomeOnboardingScreen
import com.example.widget.AppWidgetComposable

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
            .testTag("home_screen_canvas")
    ) {
        // 1. Wallpaper background (Custom Photo from Gallery OR Dynamic Theme)
        if (uiState.customWallpaperUri != null) {
            AsyncImage(
                model = uiState.customWallpaperUri,
                contentDescription = "Papel de Parede Personalizado",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(uiState.currentWallpaper.backgroundBrush)
            )
        }

        // Gesture detection for home screen canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            viewModel.handleDoubleTap()
                        },
                        onLongPress = {
                            viewModel.openSettingsPage(true)
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
                                // Swipe DOWN -> configured action
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
        )

        // Main Home Screen Layout
        AnimatedVisibility(
            visible = !uiState.isDrawerOpen && !uiState.isCustomizeSheetOpen && !uiState.isSettingsPageOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Adaptive Header
                HomeScreenHeader(
                    themeStyle = uiState.themeStyle,
                    onSearchClick = { viewModel.openSearchSheet(true) },
                    onAiRoutineClick = { viewModel.openAiRoutineModal(true) },
                    onCustomizeClick = { viewModel.openSettingsPage(true) }
                )

                // Edit Mode Indicator & Tools Banner
                if (uiState.isEditMode) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Modo de Edição Ativo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { activity?.launchWidgetPicker() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Icon(Icons.Rounded.Widgets, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Widget", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.addHomePage() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tela", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.setEditMode(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // AI Daily Routine & Suggestions Card (displayed only if AI is enabled)
                if (uiState.isAiEnabled) {
                    AiDailySuggestionsSection(
                        briefing = uiState.aiBriefing,
                        installedApps = uiState.apps.filter { !it.isHidden },
                        iconShape = uiState.iconShape,
                        iconThemed = uiState.iconThemed,
                        themeStyle = uiState.themeStyle,
                        isLoading = uiState.isAiLoading,
                        onRefreshClick = { viewModel.refreshAiSuggestions() },
                        onAppClick = { com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel) },
                        onOpenBriefingModal = { viewModel.openAiRoutineModal(true) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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

                // Primary Home Apps (Settings Shortcut + Non-dock apps)
                val homeApps = remember(uiState.apps, uiState.dockApps) {
                    val nonDockApps = uiState.apps.filter { !it.isPinnedToDock && !it.isHidden }
                    val settingsShortcut = nonDockApps.firstOrNull { it.isLauncherSettingsShortcut }
                    val regularApps = nonDockApps.filter { !it.isLauncherSettingsShortcut }
                    if (settingsShortcut != null) {
                        listOf(settingsShortcut) + regularApps
                    } else {
                        nonDockApps
                    }
                }

                val appsPerPage = uiState.gridColumns * 4
                val totalPages = maxOf(
                    uiState.homePageCount,
                    if (homeApps.isEmpty()) 1 else (homeApps.size + appsPerPage - 1) / appsPerPage
                )
                val pagerState = rememberPagerState(pageCount = { totalPages })

                // Multi-page Horizontal Pager for Home Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val startIndex = pageIndex * appsPerPage
                        val pageApps = homeApps.drop(startIndex).take(appsPerPage)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (uiState.homeScreenStyle) {
                                com.example.model.HomeScreenStyle.CLASSIC_GRID -> {
                                    val rows = pageApps.chunked(uiState.gridColumns)
                                    for (row in rows) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            for (app in row) {
                                                androidx.compose.runtime.key(app.packageName) {
                                                    AppIconComposable(
                                                        app = app,
                                                        iconShape = uiState.iconShape,
                                                        iconSizeDp = uiState.iconSizeDp,
                                                        iconThemed = uiState.iconThemed,
                                                        showLabel = uiState.showLabels,
                                                        onClick = {
                                                            if (app.isLauncherSettingsShortcut || app.packageName == "com.front.launcher.settings") {
                                                                viewModel.openSettingsPage(true)
                                                            } else {
                                                                com.example.util.AppLauncherHelper.launchAppSafely(
                                                                    activity as? androidx.fragment.app.FragmentActivity,
                                                                    app,
                                                                    viewModel
                                                                )
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (uiState.isEditMode) {
                                                                viewModel.openAppMenu(app)
                                                            } else {
                                                                viewModel.setEditMode(true)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            for (i in row.size until uiState.gridColumns) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                                com.example.model.HomeScreenStyle.MINIMALIST_TEXT -> {
                                    for (app in pageApps) {
                                        Text(
                                            text = app.label.lowercase(),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Light,
                                            color = Color.White,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clickable {
                                                    if (app.isLauncherSettingsShortcut) viewModel.openSettingsPage(true)
                                                    else viewModel.launchApp(app)
                                                }
                                        )
                                    }
                                }
                                com.example.model.HomeScreenStyle.DOCK_ONLY -> {
                                    // Empty middle area
                                }
                            }
                        }
                    }
                }

                // Page Indicator Dots (if multiple pages exist)
                if (totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(totalPages) { pageIdx ->
                            val isSelected = pagerState.currentPage == pageIdx
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }

                // Bottom Navigation Dock (up to 5 apps)
                DockBar(
                    dockApps = uiState.dockApps,
                    iconShape = uiState.iconShape,
                    iconSizeDp = uiState.iconSizeDp,
                    iconThemed = uiState.iconThemed,
                    themeStyle = uiState.themeStyle,
                    dockBlurColor = uiState.currentWallpaper.dockBlurColor,
                    onAppClick = {
                        if (it.isLauncherSettingsShortcut || it.packageName == "com.front.launcher.settings") {
                            viewModel.openSettingsPage(true)
                        } else {
                            com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel)
                        }
                    },
                    onAppLongClick = { viewModel.openAppMenu(it) },
                    onOpenDrawer = { viewModel.setDrawerOpen(true) }
                )
            }
        } // Close AnimatedVisibility Main Home

        // 2. Full Page Settings Screen (opens a brand-new page)
        AnimatedVisibility(
            visible = uiState.isSettingsPageOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            LauncherSettingsScreen(
                uiState = uiState,
                viewModel = viewModel,
                onRequestDefaultHome = { activity?.requestDefaultLauncher() },
                onClose = { viewModel.openSettingsPage(false) }
            )
        }

        // 3. App Drawer Sheet
        AnimatedVisibility(
            visible = uiState.isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerSheet(
                isOpen = uiState.isDrawerOpen,
                apps = uiState.apps.filter { !it.isHidden },
                searchQuery = uiState.searchQuery,
                drawerStyle = uiState.appDrawerStyle,
                drawerCategorized = uiState.drawerCategorized,
                selectedCategoryFilter = uiState.selectedCategoryFilter,
                iconShape = uiState.iconShape,
                iconSizeDp = uiState.iconSizeDp,
                iconThemed = uiState.iconThemed,
                showLabels = uiState.showLabels,
                themeStyle = uiState.themeStyle,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSelectCategoryFilter = { viewModel.selectCategoryFilter(it) },
                onAppClick = {
                    if (it.isLauncherSettingsShortcut || it.packageName == "com.front.launcher.settings") {
                        viewModel.openSettingsPage(true)
                    } else {
                        com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel)
                    }
                },
                onAppLongClick = { viewModel.openAppMenu(it) },
                onCloseDrawer = { viewModel.setDrawerOpen(false) }
            )
        }

        // 4. Customize Launcher Sheet (for quick sheet adjustments)
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
                drawerCategorized = uiState.drawerCategorized,
                onDrawerCategorizedChange = { viewModel.toggleDrawerCategorized() },
                addNewAppsToHome = uiState.addNewAppsToHome,
                onAddNewAppsToHomeChange = { viewModel.toggleAddNewAppsToHome() },
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
                onDockConfigClick = { viewModel.setDockConfigOpen(true) },
                onAppLockConfigClick = { viewModel.setAppLockConfigOpen(true) },
                onClose = { viewModel.openCustomizeSheet(false) }
            )
        }

        // 5. AI Daily Routine Modal
        AnimatedVisibility(
            visible = uiState.isAiRoutineModalOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AiRoutineModal(
                isOpen = uiState.isAiRoutineModalOpen,
                briefing = uiState.aiBriefing,
                isLoading = uiState.isAiLoading,
                installedApps = uiState.apps.filter { !it.isHidden },
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                onRefresh = { viewModel.refreshAiSuggestions() },
                onAppClick = { com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel) },
                onClose = { viewModel.openAiRoutineModal(false) }
            )
        }

        DockConfigDialog(
            isOpen = uiState.isDockConfigOpen,
            apps = uiState.apps.filter { !it.isHidden },
            onClose = { viewModel.setDockConfigOpen(false) },
            onTogglePin = { viewModel.togglePinAppToDock(it) }
        )

        AppLockConfigDialog(
            isOpen = uiState.isAppLockConfigOpen,
            apps = uiState.apps,
            onClose = { viewModel.setAppLockConfigOpen(false) },
            onToggleLock = { viewModel.toggleAppLock(it) },
            onToggleHide = { viewModel.toggleAppHidden(it) }
        )

        // 6. Universal Quick Search Sheet
        AnimatedVisibility(
            visible = uiState.isSearchSheetOpen,
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
        ) {
            SearchSheet(
                isOpen = uiState.isSearchSheetOpen,
                apps = uiState.apps.filter { !it.isHidden },
                searchQuery = uiState.searchQuery,
                iconShape = uiState.iconShape,
                iconThemed = uiState.iconThemed,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onAppClick = { com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel) },
                onClose = { viewModel.openSearchSheet(false) }
            )
        }

        // 7. App Long-press Menu Dialog
        AppActionDialog(
            app = uiState.selectedAppForMenu,
            iconShape = uiState.iconShape,
            iconThemed = uiState.iconThemed,
            onDismiss = { viewModel.openAppMenu(null) },
            onLaunch = { com.example.util.AppLauncherHelper.launchAppSafely(activity as? androidx.fragment.app.FragmentActivity, it, viewModel) },
            onTogglePinDock = { viewModel.togglePinAppToDock(it) },
            onChangeCategoryClick = { viewModel.openChangeCategoryDialog(true) }
        )

        // 8. Category Selection Dialog
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

        // 9. Welcome Onboarding Dialog / First Run Setup
        WelcomeOnboardingScreen(
            isOpen = uiState.isWelcomeOnboardingOpen,
            installedApps = uiState.apps,
            currentDockApps = uiState.dockApps,
            isAiEnabled = uiState.isAiEnabled,
            onToggleAi = { viewModel.setAiEnabled(it) },
            onUpdateDockApps = { viewModel.updateDockApps(it) },
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

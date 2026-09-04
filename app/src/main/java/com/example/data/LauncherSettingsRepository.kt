package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_settings")

class LauncherSettingsRepository(private val context: Context) {

    // Keys - Desktop
    private val GRID_ROWS = intPreferencesKey("grid_rows")
    private val GRID_COLUMNS = intPreferencesKey("grid_columns")
    private val ICON_SIZE_SCALE = floatPreferencesKey("icon_size_scale")
    private val SHOW_ICON_LABELS = booleanPreferencesKey("show_icon_labels")
    private val LABEL_TEXT_COLOR = intPreferencesKey("label_text_color")
    private val LOCK_DESKTOP = booleanPreferencesKey("lock_desktop")
    private val ALLOW_WIDGET_OVERLAP = booleanPreferencesKey("allow_widget_overlap")
    private val WALLPAPER_PARALLAX = booleanPreferencesKey("wallpaper_parallax")
    private val HIDE_STATUS_BAR = booleanPreferencesKey("hide_status_bar")
    private val HIDE_NAVIGATION_BAR = booleanPreferencesKey("hide_navigation_bar")
    private val DESKTOP_TRANSITION_EFFECT = stringPreferencesKey("desktop_transition_effect")

    // Keys - Dock
    private val DOCK_ENABLED = booleanPreferencesKey("dock_enabled")
    private val DOCK_ITEM_COUNT = intPreferencesKey("dock_item_count")
    private val DOCK_PAGES_COUNT = intPreferencesKey("dock_pages_count")
    private val DOCK_BACKGROUND_STYLE = stringPreferencesKey("dock_background_style")
    private val DOCK_BACKGROUND_ALPHA = floatPreferencesKey("dock_background_alpha")
    private val SHOW_DOCK_SEARCH_BAR = booleanPreferencesKey("show_dock_search_bar")

    // Keys - Drawer
    private val DRAWER_STYLE = stringPreferencesKey("drawer_style")
    private val DRAWER_COLUMNS = intPreferencesKey("drawer_columns")
    private val DRAWER_SORT_ORDER = stringPreferencesKey("drawer_sort_order")
    private val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
    private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    private val AUTO_FOCUS_SEARCH_KEYBOARD = booleanPreferencesKey("auto_focus_search_keyboard")
    private val DRAWER_BACKGROUND_BLUR = booleanPreferencesKey("drawer_background_blur")
    private val DRAWER_BACKGROUND_ALPHA = floatPreferencesKey("drawer_background_alpha")
    private val FAST_SCROLLER_ENABLED = booleanPreferencesKey("fast_scroller_enabled")

    // Keys - Appearance & Icons
    private val CURRENT_ICON_PACK_PACKAGE = stringPreferencesKey("current_icon_pack_package")
    private val ICON_SHAPE = stringPreferencesKey("icon_shape")
    private val DYNAMIC_MATERIAL_YOU_THEMING = booleanPreferencesKey("dynamic_material_you_theming")
    private val NOTIFICATION_BADGES = stringPreferencesKey("notification_badges")
    private val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode")

    // Keys - Gestures
    private val GESTURE_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
    private val GESTURE_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
    private val GESTURE_DOUBLE_TAP_DESKTOP = stringPreferencesKey("gesture_double_tap_desktop")
    private val GESTURE_PINCH_IN = stringPreferencesKey("gesture_pinch_in")
    private val GESTURE_PINCH_OUT = stringPreferencesKey("gesture_pinch_out")
    private val GESTURE_TWO_FINGER_SWIPE_DOWN = stringPreferencesKey("gesture_two_finger_swipe_down")

    // Keys - Search
    private val SEARCH_ENGINE_PROVIDER = stringPreferencesKey("search_engine_provider")
    private val SEARCH_TARGET = stringPreferencesKey("search_target")

    // Exposing Flows for Settings State
    val gridRowsFlow: Flow<Int> = context.dataStore.data.map { it[GRID_ROWS] ?: 5 }
    val gridColumnsFlow: Flow<Int> = context.dataStore.data.map { it[GRID_COLUMNS] ?: 4 }
    val iconSizeScaleFlow: Flow<Float> = context.dataStore.data.map { it[ICON_SIZE_SCALE] ?: 1.0f }
    val showIconLabelsFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_ICON_LABELS] ?: true }
    val lockDesktopFlow: Flow<Boolean> = context.dataStore.data.map { it[LOCK_DESKTOP] ?: false }
    val dockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[DOCK_ENABLED] ?: true }
    val hiddenPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { it[HIDDEN_PACKAGES] ?: emptySet() }
    val currentIconPackPackageFlow: Flow<String?> = context.dataStore.data.map { it[CURRENT_ICON_PACK_PACKAGE] }

    val iconShapeFlow: Flow<IconShape> = context.dataStore.data.map { 
        IconShape.valueOf(it[ICON_SHAPE] ?: IconShape.SQUIRCLE.name) 
    }
    
    val drawerStyleFlow: Flow<AppDrawerStyle> = context.dataStore.data.map { 
        AppDrawerStyle.valueOf(it[DRAWER_STYLE] ?: AppDrawerStyle.VERTICAL_GRID.name) 
    }

    // Update Functions
    suspend fun updateGridSettings(rows: Int, cols: Int) {
        context.dataStore.edit { 
            it[GRID_ROWS] = rows
            it[GRID_COLUMNS] = cols
        }
    }

    suspend fun updateIconSizeScale(scale: Float) {
        context.dataStore.edit { it[ICON_SIZE_SCALE] = scale }
    }

    suspend fun updateShowIconLabels(show: Boolean) {
        context.dataStore.edit { it[SHOW_ICON_LABELS] = show }
    }

    suspend fun updateLockDesktop(locked: Boolean) {
        context.dataStore.edit { it[LOCK_DESKTOP] = locked }
    }

    suspend fun updateDockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DOCK_ENABLED] = enabled }
    }

    suspend fun addHiddenPackage(pkg: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_PACKAGES] ?: emptySet()
            prefs[HIDDEN_PACKAGES] = current + pkg
        }
    }

    suspend fun removeHiddenPackage(pkg: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_PACKAGES] ?: emptySet()
            prefs[HIDDEN_PACKAGES] = current - pkg
        }
    }

    suspend fun updateIconPack(pkg: String?) {
        context.dataStore.edit { 
            if (pkg == null) {
                it.remove(CURRENT_ICON_PACK_PACKAGE)
            } else {
                it[CURRENT_ICON_PACK_PACKAGE] = pkg
            }
        }
    }

    suspend fun updateIconShape(shape: IconShape) {
        context.dataStore.edit { it[ICON_SHAPE] = shape.name }
    }

    suspend fun updateDrawerStyle(style: AppDrawerStyle) {
        context.dataStore.edit { it[DRAWER_STYLE] = style.name }
    }

    // Advanced: Export/Import implementation can serialize current Preferences to JSON
}

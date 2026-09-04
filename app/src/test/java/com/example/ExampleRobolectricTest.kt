package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppCategory
import com.example.model.IconShape
import com.example.model.LauncherThemeStyle
import com.example.ui.theme.WallpaperThemeEngine
import com.example.util.CategoryClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Home Launcher", appName)
  }

  @Test
  fun `verify category classifier for common apps`() {
    assertEquals(
      AppCategory.COMMUNICATION,
      CategoryClassifier.classify("com.whatsapp", "WhatsApp")
    )
    assertEquals(
      AppCategory.PRODUCTIVITY,
      CategoryClassifier.classify("com.google.android.gm", "Gmail")
    )
    assertEquals(
      AppCategory.FINANCE,
      CategoryClassifier.classify("com.nu.production", "Nubank")
    )
    assertEquals(
      AppCategory.GAMES,
      CategoryClassifier.classify("com.supercell.clashroyale", "Clash Royale")
    )
  }

  @Test
  fun `verify wallpaper theme engine contains presets`() {
    val wallpapers = WallpaperThemeEngine.wallpaperList
    assertTrue(wallpapers.size >= 4)
    val monet = WallpaperThemeEngine.getThemeById("monet_aurora")
    assertNotNull(monet)
    assertEquals("Monet Aurora", monet.name)
  }

  @Test
  fun `verify icon shapes and launcher styles exist`() {
    assertTrue(IconShape.entries.contains(IconShape.SQUIRCLE))
    assertTrue(IconShape.entries.contains(IconShape.ROUNDED_SQUARE))
    assertTrue(LauncherThemeStyle.entries.contains(LauncherThemeStyle.MATERIAL_YOU))
    assertTrue(LauncherThemeStyle.entries.contains(LauncherThemeStyle.ONE_UI))
    assertTrue(LauncherThemeStyle.entries.contains(LauncherThemeStyle.HYPER_OS))
  }
}


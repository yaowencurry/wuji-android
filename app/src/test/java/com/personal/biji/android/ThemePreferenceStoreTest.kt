package com.personal.biji.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.personal.biji.android.ui.ThemePreference
import com.personal.biji.android.ui.ThemePreferenceStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemePreferenceStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun clearPreferences() {
        context.getSharedPreferences("biji_theme_preference", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun defaultsToSystem() {
        assertEquals(ThemePreference.System, ThemePreferenceStore(context).preference.value)
    }

    @Test
    fun persistsThemeSelection() {
        ThemePreferenceStore(context).setPreference(ThemePreference.Dark)
        assertEquals(ThemePreference.Dark, ThemePreferenceStore(context).preference.value)

        ThemePreferenceStore(context).setPreference(ThemePreference.Light)
        assertEquals(ThemePreference.Light, ThemePreferenceStore(context).preference.value)
    }

    @Test
    fun invalidStoredValueFallsBackToSystem() {
        context.getSharedPreferences("biji_theme_preference", Context.MODE_PRIVATE)
            .edit()
            .putString("preference", "invalid")
            .commit()

        assertEquals(ThemePreference.System, ThemePreferenceStore(context).preference.value)
    }
}

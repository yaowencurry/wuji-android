package com.personal.biji.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.personal.biji.android.ui.FontPreference
import com.personal.biji.android.ui.FontPreferenceStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FontPreferenceStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun clearPreferences() {
        context.getSharedPreferences("biji_font_preference", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun defaultsToLxgwWenKai() {
        assertEquals(FontPreference.LxgwWenKai, FontPreferenceStore(context).preference.value)
    }

    @Test
    fun persistsSystemFontSelection() {
        FontPreferenceStore(context).setPreference(FontPreference.System)

        assertEquals(FontPreference.System, FontPreferenceStore(context).preference.value)
    }
}

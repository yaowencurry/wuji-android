package com.personal.biji.android.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FontPreference(val title: String) {
    LxgwWenKai("霞鹜文楷"),
    System("系统字体"),
}

class FontPreferenceStore(context: Context) {
    private val preferences = context.getSharedPreferences("biji_font_preference", Context.MODE_PRIVATE)
    private val mutablePreference = MutableStateFlow(preferences.getString(KEY_PREFERENCE, null).toFontPreference())

    val preference: StateFlow<FontPreference> = mutablePreference.asStateFlow()

    fun setPreference(preference: FontPreference) {
        preferences.edit().putString(KEY_PREFERENCE, preference.name).apply()
        mutablePreference.value = preference
    }

    private fun String?.toFontPreference(): FontPreference =
        runCatching { this?.let(FontPreference::valueOf) }.getOrNull() ?: FontPreference.LxgwWenKai

    private companion object {
        const val KEY_PREFERENCE = "preference"
    }
}

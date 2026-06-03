package com.personal.biji.android.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePreference(val title: String) {
    System("跟随系统"),
    Light("浅色"),
    Dark("深色"),
}

class ThemePreferenceStore(context: Context) {
    private val preferences = context.getSharedPreferences("biji_theme_preference", Context.MODE_PRIVATE)
    private val mutablePreference = MutableStateFlow(preferences.getString(KEY_PREFERENCE, null).toThemePreference())

    val preference: StateFlow<ThemePreference> = mutablePreference.asStateFlow()

    fun setPreference(preference: ThemePreference) {
        preferences.edit().putString(KEY_PREFERENCE, preference.name).apply()
        mutablePreference.value = preference
    }

    private fun String?.toThemePreference(): ThemePreference =
        runCatching { this?.let(ThemePreference::valueOf) }.getOrNull() ?: ThemePreference.System

    private companion object {
        const val KEY_PREFERENCE = "preference"
    }
}

package com.personal.biji.android.ui

import android.content.Context

internal class QuickCreateStore(context: Context) {
    private val preferences = context.getSharedPreferences("biji_quick_create", Context.MODE_PRIVATE)

    fun processedFingerprints(): Set<String> =
        history().toSet()

    fun consume(candidates: List<QuickCreateCandidate>) {
        val updated = trimQuickCreateHistory(history(), candidates.map { it.fingerprint })
        preferences.edit().putString(KEY_PROCESSED, updated.joinToString("\n")).apply()
    }

    fun hasAskedForPhotoPermission(): Boolean =
        preferences.getBoolean(KEY_ASKED_FOR_PHOTO_PERMISSION, false)

    fun markPhotoPermissionAsked() {
        preferences.edit().putBoolean(KEY_ASKED_FOR_PHOTO_PERMISSION, true).apply()
    }

    private fun history(): List<String> =
        preferences.getString(KEY_PROCESSED, null)
            ?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.toList()
            .orEmpty()

    private companion object {
        const val KEY_PROCESSED = "processed"
        const val KEY_ASKED_FOR_PHOTO_PERMISSION = "asked_for_photo_permission"
    }
}

package com.personal.biji.android.data

import android.content.Context
import com.personal.biji.android.domain.StorageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StorageModeStore {
    val mode: StateFlow<StorageMode>
    suspend fun setMode(mode: StorageMode)
}

class SharedPreferencesStorageModeStore(context: Context) : StorageModeStore {
    private val preferences = context.getSharedPreferences("biji_storage", Context.MODE_PRIVATE)
    private val mutableMode = MutableStateFlow(preferences.getString(KEY_MODE, null).toStorageMode())
    override val mode: StateFlow<StorageMode> = mutableMode.asStateFlow()

    override suspend fun setMode(mode: StorageMode) {
        preferences.edit().putString(KEY_MODE, mode.name).apply()
        mutableMode.value = mode
    }

    private fun String?.toStorageMode(): StorageMode =
        runCatching { this?.let(StorageMode::valueOf) }.getOrNull() ?: StorageMode.Local

    private companion object {
        const val KEY_MODE = "mode"
    }
}

class InMemoryStorageModeStore(initialMode: StorageMode = StorageMode.Local) : StorageModeStore {
    private val mutableMode = MutableStateFlow(initialMode)
    override val mode: StateFlow<StorageMode> = mutableMode.asStateFlow()

    override suspend fun setMode(mode: StorageMode) {
        mutableMode.value = mode
    }
}

package com.personal.biji.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.personal.biji.android.data.LibraryRepositoryImpl
import com.personal.biji.android.data.SharedPreferencesStorageModeStore
import com.personal.biji.android.data.local.BijiDatabase
import com.personal.biji.android.data.remote.ApiFactory
import com.personal.biji.android.ui.BijiApp
import com.personal.biji.android.ui.MlKitOcrTextRecognizer
import com.personal.biji.android.ui.parseLaunchRequest
import com.personal.biji.android.upload.DirectTosImageUploader
import com.personal.biji.android.upload.LocalImageUploader
import com.personal.biji.android.upload.ModeAwareImageUploader
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainActivity : ComponentActivity() {
    private val launchRequests = Channel<com.personal.biji.android.ui.AppLaunchRequest>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseLaunchRequest(intent)?.let(launchRequests::trySend)
        val database = BijiDatabase.create(applicationContext)
        val api = ApiFactory.create(BuildConfig.BIJI_API_BASE_URL)
        val storageModeStore = SharedPreferencesStorageModeStore(applicationContext)
        val repository = LibraryRepositoryImpl(database.libraryDao(), api, ApiFactory.gson, storageModeStore)
        val imageUploader = ModeAwareImageUploader(repository.storageMode, LocalImageUploader(applicationContext), DirectTosImageUploader(api))
        setContent {
            BijiApp(
                repository = repository,
                imageUploader = imageUploader,
                launchRequests = launchRequests.receiveAsFlow(),
                ocrRecognizer = MlKitOcrTextRecognizer(applicationContext),
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseLaunchRequest(intent)?.let(launchRequests::trySend)
    }
}

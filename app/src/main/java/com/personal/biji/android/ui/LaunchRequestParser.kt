package com.personal.biji.android.ui

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

sealed interface AppLaunchRequest {
    data object NewTextNote : AppLaunchRequest
    data object ScanFromCamera : AppLaunchRequest
    data object ScanFromGallery : AppLaunchRequest
    data class SharedText(val value: String) : AppLaunchRequest
    data class SharedImage(val uri: Uri) : AppLaunchRequest
}

const val ACTION_NEW_TEXT_NOTE = "com.personal.biji.android.action.NEW_TEXT_NOTE"
const val ACTION_SCAN_CAMERA = "com.personal.biji.android.action.SCAN_CAMERA"
const val ACTION_SCAN_GALLERY = "com.personal.biji.android.action.SCAN_GALLERY"

fun parseLaunchRequest(intent: Intent?): AppLaunchRequest? =
    when (intent?.action) {
        ACTION_NEW_TEXT_NOTE -> AppLaunchRequest.NewTextNote
        ACTION_SCAN_CAMERA -> AppLaunchRequest.ScanFromCamera
        ACTION_SCAN_GALLERY -> AppLaunchRequest.ScanFromGallery
        Intent.ACTION_SEND -> when {
            intent.type?.startsWith("text/") == true ->
                intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
                    ?.toString()
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let(AppLaunchRequest::SharedText)
            intent.type?.startsWith("image/") == true ->
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                    ?.let(AppLaunchRequest::SharedImage)
            else -> null
        }
        else -> null
    }

package com.personal.biji.android.ui

import android.content.Context
import android.net.Uri

fun mimeTypeForUri(context: Context, uri: Uri): String =
    context.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"

fun fileExtensionForMimeType(mimeType: String?): String =
    when (mimeType?.lowercase()) {
        "image/png" -> "png"
        "image/jpeg", "image/jpg" -> "jpg"
        else -> "jpg"
    }

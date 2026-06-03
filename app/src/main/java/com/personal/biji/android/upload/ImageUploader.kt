package com.personal.biji.android.upload

import android.content.Context
import android.net.Uri
import com.personal.biji.android.data.remote.BijiApi
import com.personal.biji.android.data.remote.DirectUploadInitRequest
import com.personal.biji.android.domain.ImageUploadResponse
import com.personal.biji.android.domain.StorageMode
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.UUID

interface ImageUploader {
    suspend fun upload(data: ByteArray, contentType: String = "image/jpeg"): ImageUploadResponse
}

class DirectTosImageUploader(
    private val api: BijiApi,
    private val signer: TosSigner = TosV4Signer(),
    private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build(),
) : ImageUploader {
    override suspend fun upload(data: ByteArray, contentType: String): ImageUploadResponse {
        val extension = if (contentType == "image/png") "png" else "jpg"
        val session = api.initDirectUpload(DirectUploadInitRequest(contentType, data.size.toLong(), extension))
        client.newCall(signer.signedPut(data, contentType, session)).execute().use {
            check(it.isSuccessful) { "TOS upload failed: ${it.code}" }
        }
        return ImageUploadResponse(session.url, session.objectKey)
    }
}

class LocalImageUploader(
    context: Context,
) : ImageUploader {
    private val directory = File(context.filesDir, "note-images").apply { mkdirs() }

    override suspend fun upload(data: ByteArray, contentType: String): ImageUploadResponse {
        val extension = if (contentType == "image/png") "png" else "jpg"
        val file = File(directory, "${UUID.randomUUID()}.$extension")
        file.writeBytes(data)
        val url = Uri.fromFile(file).toString()
        return ImageUploadResponse(url = url, objectKey = "local/${file.name}")
    }
}

class ModeAwareImageUploader(
    private val storageMode: StateFlow<StorageMode>,
    private val local: ImageUploader,
    private val remote: ImageUploader,
) : ImageUploader {
    override suspend fun upload(data: ByteArray, contentType: String): ImageUploadResponse =
        when (storageMode.value) {
            StorageMode.Local -> local.upload(data, contentType)
            StorageMode.Remote -> remote.upload(data, contentType)
        }
}

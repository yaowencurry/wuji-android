package com.personal.biji.android

import android.content.Intent
import android.net.Uri
import com.personal.biji.android.ui.AppLaunchRequest
import com.personal.biji.android.ui.parseLaunchRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LaunchRequestParserTest {
    @Test
    fun parsesSharedTextAndTrimsWhitespace() {
        val request = parseLaunchRequest(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "  一段摘抄  ")
            },
        )

        assertEquals(AppLaunchRequest.SharedText("一段摘抄"), request)
    }

    @Test
    fun parsesSingleSharedImage() {
        val uri = Uri.parse("content://images/42")
        val request = parseLaunchRequest(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
            },
        )

        assertEquals(AppLaunchRequest.SharedImage(uri), request)
    }

    @Test
    fun ignoresBlankTextAndUnsupportedMimeTypes() {
        assertNull(parseLaunchRequest(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "  ")
        }))
        assertNull(parseLaunchRequest(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
        }))
    }

    @Test
    fun parsesWidgetActions() {
        assertEquals(
            AppLaunchRequest.NewTextNote,
            parseLaunchRequest(Intent("com.personal.biji.android.action.NEW_TEXT_NOTE")),
        )
        assertEquals(
            AppLaunchRequest.ScanFromCamera,
            parseLaunchRequest(Intent("com.personal.biji.android.action.SCAN_CAMERA")),
        )
        assertEquals(
            AppLaunchRequest.ScanFromGallery,
            parseLaunchRequest(Intent("com.personal.biji.android.action.SCAN_GALLERY")),
        )
    }
}

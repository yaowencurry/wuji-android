package com.personal.biji.android

import com.personal.biji.android.ui.fileExtensionForMimeType
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageInputTest {
    @Test
    fun fileExtensionForMimeTypeSupportsJpegAndPng() {
        assertEquals("jpg", fileExtensionForMimeType("image/jpeg"))
        assertEquals("jpg", fileExtensionForMimeType("image/jpg"))
        assertEquals("png", fileExtensionForMimeType("image/png"))
        assertEquals("jpg", fileExtensionForMimeType(null))
    }
}

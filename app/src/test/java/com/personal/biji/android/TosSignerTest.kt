package com.personal.biji.android

import com.personal.biji.android.domain.DirectImageUploadSession
import com.personal.biji.android.domain.TosTemporaryCredentials
import com.personal.biji.android.upload.TosV4Signer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TosSignerTest {
    @Test
    fun signedPutIncludesInlineDispositionAndTemporaryToken() {
        val request = TosV4Signer().signedPut(
            data = "jpeg".toByteArray(),
            contentType = "image/jpeg",
            session = DirectImageUploadSession(
                bucket = "biji-notes",
                region = "cn-beijing",
                endpoint = "tos-cn-beijing.volces.com",
                objectKey = "notes/images/mock/image.jpg",
                url = "https://cdn.example.com/image.jpg",
                expiresAt = "2026-05-27T10:00:00Z",
                credentials = TosTemporaryCredentials("sts-ak", "sts-sk", "sts-token"),
            ),
            now = Instant.parse("2026-05-27T09:46:40Z"),
        )
        assertEquals("inline", request.header("Content-Disposition"))
        assertEquals("sts-token", request.header("X-Tos-Security-Token"))
        assertTrue(request.header("Authorization")!!.contains("Credential=sts-ak/20260527/cn-beijing/tos/request"))
    }
}

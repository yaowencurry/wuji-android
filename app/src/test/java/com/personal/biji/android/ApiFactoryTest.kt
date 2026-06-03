package com.personal.biji.android

import com.personal.biji.android.data.remote.ApiFactory
import com.personal.biji.android.domain.Note
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiFactoryTest {
    @Test
    fun decodesCrossPlatformRichTextDocument() {
        val note = ApiFactory.gson.fromJson(
            """{"id":"note","bookId":"book","content":"重点","richTextDocument":{"version":1,"blocks":[{"type":"paragraph","runs":[{"text":"重点","marks":[{"type":"bold"}]}]}]},"createdAt":"2026-05-27T08:00:00Z","updatedAt":"2026-05-27T08:00:00Z"}""",
            Note::class.java,
        )
        assertEquals("bold", note.richTextDocument!!.blocks.single().runs.single().marks.single().type)
    }
}

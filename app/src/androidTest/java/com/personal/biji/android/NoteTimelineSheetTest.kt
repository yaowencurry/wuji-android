package com.personal.biji.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.personal.biji.android.domain.Note
import com.personal.biji.android.ui.BijiTheme
import com.personal.biji.android.ui.FontPreference
import com.personal.biji.android.ui.NoteTimelineSheet
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class NoteTimelineSheetTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sheetShowsCreatedAndLastEditedTimes() {
        compose.setContent {
            BijiTheme(FontPreference.System) {
                NoteTimelineSheet(
                    Note(
                        bookId = "book",
                        content = "笔记",
                        createdAt = Instant.parse("2026-06-02T10:00:00Z"),
                        updatedAt = Instant.parse("2026-06-02T11:00:00Z"),
                    ),
                ) {}
            }
        }

        compose.onNodeWithText("时间线").assertIsDisplayed()
        compose.onNodeWithText("创建").assertIsDisplayed()
        compose.onNodeWithText("最后编辑").assertIsDisplayed()
    }
}

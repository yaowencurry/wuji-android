package com.personal.biji.android.ui

import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun noteDisplaySummary(note: Note): String {
    val cleaned = note.content
        .replace("[图片]", "")
        .trim()
    return cleaned.ifEmpty {
        if (noteImages(note).isNotEmpty()) "图片笔记" else ""
    }
}

internal fun noteImages(note: Note): List<NoteContentBlock> {
    val blockImages = note.contentBlocks.filter { it.type == "image" && !it.url.isNullOrBlank() }
    val richTextImages = note.richTextDocument?.blocks.orEmpty()
        .mapNotNull { it.image }
        .filter { it.type == "image" && !it.url.isNullOrBlank() }
    return (blockImages + richTextImages).distinctBy { it.url }
}

internal fun noteMonthTitle(date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val local = date.atZone(zoneId)
    return "${local.year}年${local.monthValue}月"
}

internal fun noteDayText(date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
    date.atZone(zoneId).dayOfMonth.toString().padStart(2, '0')

internal fun noteMonthText(date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
    "${date.atZone(zoneId).monthValue}月"

internal fun noteTimeText(date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern("HH:mm").withZone(zoneId).format(date)

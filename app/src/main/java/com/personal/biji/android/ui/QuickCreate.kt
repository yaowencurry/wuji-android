package com.personal.biji.android.ui

import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.editor.EditorBlock
import com.personal.biji.android.editor.insertImageBlock
import java.security.MessageDigest

sealed interface QuickCreateSeed {
    data class Text(val value: String) : QuickCreateSeed
    data class Image(val image: NoteContentBlock) : QuickCreateSeed
}

data class QuickCreateCandidate(
    val seed: QuickCreateSeed,
    val fingerprint: String,
    val timestampMillis: Long,
)

data class RandomPrompt(val noteId: String, val text: String)

fun randomPromptFromNotes(notes: List<Note>): RandomPrompt? =
    notes.asSequence()
        .sortedByDescending { it.updatedAt }
        .mapNotNull { note ->
            val text = noteDisplaySummary(note)
            if (text.isBlank() || text == "图片笔记") null else RandomPrompt(note.id, text)
        }
        .firstOrNull()

fun quickCreatePreview(seed: QuickCreateSeed): String =
    when (seed) {
        is QuickCreateSeed.Text -> seed.value.trim().let { text ->
            if (text.length > 30) text.take(30) + "..." else text
        }
        is QuickCreateSeed.Image -> "图片"
    }

fun seedEditorBlocks(seed: QuickCreateSeed?): List<EditorBlock>? =
    when (seed) {
        null -> null
        is QuickCreateSeed.Text -> listOf(EditorBlock.Text(text = seed.value))
        is QuickCreateSeed.Image -> insertImageBlock(emptyList(), seed.image)
    }

fun textQuickCreateCandidate(value: String, timestampMillis: Long): QuickCreateCandidate? {
    val normalized = value.trim()
    if (normalized.isEmpty()) return null
    return QuickCreateCandidate(
        seed = QuickCreateSeed.Text(normalized),
        fingerprint = fingerprint("text:$normalized"),
        timestampMillis = timestampMillis,
    )
}

fun imageQuickCreateCandidate(uri: String, timestampMillis: Long): QuickCreateCandidate =
    QuickCreateCandidate(
        seed = QuickCreateSeed.Image(NoteContentBlock(type = "image", url = uri, altText = "最近图片")),
        fingerprint = fingerprint("image:$uri:$timestampMillis"),
        timestampMillis = timestampMillis,
    )

fun selectQuickCreateCandidate(
    candidates: List<QuickCreateCandidate>,
    processedFingerprints: Set<String>,
    nowMillis: Long,
    maxAgeMillis: Long = 30_000,
): QuickCreateCandidate? =
    candidates
        .asSequence()
        .filter { nowMillis - it.timestampMillis in 0..maxAgeMillis }
        .filterNot { it.fingerprint in processedFingerprints }
        .maxByOrNull { it.timestampMillis }

fun trimQuickCreateHistory(
    existing: List<String>,
    consumed: List<String>,
    limit: Int = 100,
): List<String> =
    (existing - consumed.toSet() + consumed)
        .distinct()
        .takeLast(limit)

private fun fingerprint(value: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

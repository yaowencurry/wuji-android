package com.personal.biji.android.ui

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.editor.EditorBlock
import com.personal.biji.android.editor.insertImageBlock
import kotlinx.coroutines.tasks.await

data class OcrParagraph(
    val id: String,
    val text: String,
    val selected: Boolean = true,
)

interface OcrTextRecognizer {
    suspend fun recognize(uri: Uri): List<OcrParagraph>
}

class MlKitOcrTextRecognizer(
    private val context: Context,
) : OcrTextRecognizer {
    override suspend fun recognize(uri: Uri): List<OcrParagraph> {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        return try {
            ocrParagraphs(recognizer.process(image).await().textBlocks.map { it.text })
        } finally {
            recognizer.close()
        }
    }
}

fun ocrParagraphs(values: List<String>): List<OcrParagraph> =
    values.map(String::trim)
        .filter(String::isNotEmpty)
        .mapIndexed { index, text -> OcrParagraph(id = "ocr-$index", text = text) }

fun selectedOcrText(paragraphs: List<OcrParagraph>): String =
    paragraphs.filter(OcrParagraph::selected)
        .joinToString("\n\n") { it.text.trim() }
        .trim()

fun ocrEditorBlocks(text: String, image: NoteContentBlock? = null): List<EditorBlock> {
    val blocks = text.trim().takeIf(String::isNotEmpty)
        ?.let { listOf<EditorBlock>(EditorBlock.Text(text = it)) }
        .orEmpty()
    return if (image == null) blocks else insertImageBlock(blocks, image)
}

fun appendOcrEditorBlocks(existing: List<EditorBlock>, added: List<EditorBlock>): List<EditorBlock> =
    if (added.isEmpty()) existing else existing + added

package com.personal.biji.android.editor

import android.graphics.Typeface
import android.graphics.Color
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.text.style.ForegroundColorSpan
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.URLSpan
import android.text.InputType
import android.os.Build
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.personal.biji.android.R
import com.personal.biji.android.domain.RichTextBlock
import com.personal.biji.android.domain.RichTextDocument
import com.personal.biji.android.domain.RichTextMark
import com.personal.biji.android.domain.RichTextRun
import com.personal.biji.android.ui.FontPreference

enum class TextFormat { Heading, Bold, Italic, Underline, Quote, Bullet, Numbered, Link, Color, Highlight }

@Composable
fun SpannableEditor(
    document: RichTextDocument,
    command: TextFormat?,
    fontPreference: FontPreference,
    textColor: Int,
    hintColor: Int,
    modifier: Modifier = Modifier,
    onCommandApplied: () -> Unit,
    onDocumentChange: (RichTextDocument) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            EditText(context).apply {
                hint = "写下你的读书笔记..."
                gravity = android.view.Gravity.TOP
                setPadding(32, 12, 32, 12)
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(textColor)
                setHintTextColor(hintColor)
                typeface = editorTypeface(context, fontPreference)
                textSize = 14f
                setLineSpacing(0f, 1f)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                isSingleLine = false
                maxLines = Int.MAX_VALUE
                setHorizontallyScrolling(false)
                isVerticalScrollBarEnabled = false
                overScrollMode = android.view.View.OVER_SCROLL_NEVER
                if (Build.VERSION.SDK_INT >= 21) isNestedScrollingEnabled = false
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    override fun afterTextChanged(s: Editable?) {
                        s?.let { onDocumentChange(documentFromEditable(it)) }
                    }
                })
            }
        },
        update = { editText ->
            editText.setTextColor(textColor)
            editText.setHintTextColor(hintColor)
            editText.typeface = editorTypeface(editText.context, fontPreference)
            val expected = summary(document)
            if (editText.text.toString() != expected) {
                editText.setText(spannableFromDocument(document))
                editText.setSelection(editText.text.length)
            }
            command?.let {
                applyFormat(editText, it)
                onCommandApplied()
            }
        },
    )
}

private fun editorTypeface(context: android.content.Context, preference: FontPreference): Typeface =
    if (preference == FontPreference.LxgwWenKai) {
        ResourcesCompat.getFont(context, R.font.lxgw_wenkai_gb_screen) ?: Typeface.DEFAULT
    } else {
        Typeface.DEFAULT
    }

private fun spannableFromDocument(document: RichTextDocument): SpannableString {
    val value = SpannableString(summary(document))
    var cursor = 0
    document.blocks.forEachIndexed { blockIndex, block ->
        block.runs.forEach { run ->
            val end = cursor + run.text.length
            run.marks.forEach { mark ->
                val span = when (mark.type) {
                    "bold" -> StyleSpan(Typeface.BOLD)
                    "italic" -> StyleSpan(Typeface.ITALIC)
                    "underline" -> UnderlineSpan()
                    "link" -> URLSpan(mark.value ?: "https://example.com")
                    "color" -> ForegroundColorSpan(0xFF1F9F58.toInt())
                    "highlight" -> BackgroundColorSpan(0xFFE8F8EF.toInt())
                    "heading" -> RelativeSizeSpan(1.35f)
                    else -> null
                }
                span?.let { value.setSpan(it, cursor, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
            }
            cursor = end
        }
        if (blockIndex < document.blocks.lastIndex) cursor++
    }
    return value
}

private fun documentFromEditable(editable: Editable): RichTextDocument {
    if (editable.isEmpty()) return plainDocument("")
    val boundaries = sortedSetOf(0, editable.length)
    editable.getSpans(0, editable.length, Any::class.java).forEach {
        boundaries += editable.getSpanStart(it).coerceAtLeast(0)
        boundaries += editable.getSpanEnd(it).coerceAtMost(editable.length)
    }
    val points = boundaries.toList()
    val runs = points.zipWithNext().mapNotNull { (start, end) ->
        if (start == end) return@mapNotNull null
        RichTextRun(editable.substring(start, end), editable.getSpans(start, end, Any::class.java).mapNotNull { span ->
            when (span) {
                is StyleSpan -> if (span.style == Typeface.BOLD) RichTextMark("bold") else RichTextMark("italic")
                is UnderlineSpan -> RichTextMark("underline")
                is URLSpan -> RichTextMark("link", span.url)
                is ForegroundColorSpan -> RichTextMark("color", "#1F9F58")
                is BackgroundColorSpan -> RichTextMark("highlight", "#E8F8EF")
                is RelativeSizeSpan -> RichTextMark("heading")
                else -> null
            }
        })
    }
    return RichTextDocument(blocks = listOf(RichTextBlock("paragraph", runs)))
}

private fun applyFormat(editor: EditText, format: TextFormat) {
    val start = editor.selectionStart.coerceAtLeast(0)
    val end = editor.selectionEnd.coerceAtLeast(start)
    if (start == end) return
    val span = when (format) {
        TextFormat.Heading -> RelativeSizeSpan(1.35f)
        TextFormat.Bold -> StyleSpan(Typeface.BOLD)
        TextFormat.Italic -> StyleSpan(Typeface.ITALIC)
        TextFormat.Underline -> UnderlineSpan()
        TextFormat.Link -> URLSpan("https://example.com")
        TextFormat.Color -> ForegroundColorSpan(0xFF1F9F58.toInt())
        TextFormat.Highlight -> BackgroundColorSpan(0xFFE8F8EF.toInt())
        TextFormat.Quote, TextFormat.Bullet, TextFormat.Numbered -> {
            val prefix = when (format) {
                TextFormat.Quote -> "> "
                TextFormat.Bullet -> "• "
                else -> "1. "
            }
            editor.text.insert(start, prefix)
            return
        }
    }
    editor.text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun plainDocument(text: String): RichTextDocument = RichTextDocument(
    blocks = listOf(RichTextBlock("paragraph", runs = listOf(RichTextRun(text, emptyList()))))
)

fun summary(document: RichTextDocument): String = document.blocks
    .mapNotNull { block ->
        if (block.image != null) null else block.runs.joinToString("") { it.text }
    }
    .joinToString("\n")

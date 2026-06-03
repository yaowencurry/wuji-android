package com.personal.biji.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.Tag
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal const val TIMELINE_TIME_COLUMN_MIN_HEIGHT_DP = 44
internal const val TIMELINE_CARD_MIN_HEIGHT_DP = 92

@Composable
internal fun PageHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow, color = BrandDeep, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(title, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = InkMuted, fontSize = 13.sp, lineHeight = 18.sp)
        }
        action()
    }
}

@Composable
internal fun CompactPageHeader(
    title: String,
    subtitle: String? = null,
    action: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, color = InkMuted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 3.dp)) }
        }
        action()
    }
}

@Composable
internal fun ReadingHero(noteCount: Int, bookCount: Int) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = HeroSurface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Surface(color = Brand, shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Default.EditNote, null, tint = Color.White, modifier = Modifier.padding(8.dp).size(18.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("留住阅读里闪光的片刻", color = HeroText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("慢慢读，认真记。你的想法会在这里长成自己的书架。", color = HeroMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                HeroMetric(noteCount.toString(), "篇笔记")
                HeroMetric(bookCount.toString(), "本书")
            }
        }
    }
}

@Composable
private fun HeroMetric(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, color = Brand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = HeroMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
    }
}

@Composable
internal fun SectionLabel(title: String, hint: String? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        hint?.let { Text(it, color = InkMuted, fontSize = 12.sp) }
    }
}

@Composable
internal fun NoteCard(note: Note, book: Book?, tags: List<Tag> = emptyList(), showAllTags: (List<Tag>) -> Unit = {}, onClick: () -> Unit) {
    val images = noteImages(note)
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(noteDisplaySummary(note).ifBlank { "还没有正文" }, color = Ink, fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, maxLines = if (images.isEmpty()) 3 else 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(book?.title ?: "未命名书籍", color = InkMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(NOTE_DATE.format(note.updatedAt), color = InkMuted, fontSize = 10.sp)
                    if (note.isFavorite) Text("  收藏", color = BrandDeep, fontSize = 10.sp)
                }
                NoteTagRow(tags, showAllTags)
            }
            images.firstOrNull()?.let {
                NoteThumbnail(it, Modifier.padding(start = 10.dp))
            }
        }
    }
}

@Composable
internal fun NoteTagRow(tags: List<Tag>, showAllTags: (List<Tag>) -> Unit) {
    if (tags.isEmpty()) return
    val visible = tags.take(3)
    val hidden = tags.size - visible.size
    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        visible.forEach { tag -> BijiChip(tag.name, selected = true) {} }
        if (hidden > 0) BijiChip("+$hidden", selected = false) { showAllTags(tags) }
    }
}

@Composable
internal fun TimelineNoteCard(note: Note, book: Book?, onClick: () -> Unit) {
    val images = noteImages(note)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 3.dp).height(IntrinsicSize.Min).clickable(onClick = onClick),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(42.dp).fillMaxHeight(),
        ) {
            Text(noteMonthText(note.updatedAt), color = InkMuted, fontSize = 11.sp)
            Text(noteDayText(note.updatedAt), color = BrandDeep, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(noteTimeText(note.updatedAt), color = InkMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(OutlineSoft))
        Surface(
            modifier = Modifier.weight(1f).padding(start = 10.dp).heightIn(min = TIMELINE_CARD_MIN_HEIGHT_DP.dp),
            color = Surface,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 0.dp,
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(noteDisplaySummary(note).ifBlank { "还没有正文" }, color = Ink, fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, maxLines = if (images.isEmpty()) 3 else 2, overflow = TextOverflow.Ellipsis)
                    Text(book?.title ?: "未命名书籍", color = InkMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
                }
                images.firstOrNull()?.let { NoteThumbnail(it, Modifier.padding(start = 10.dp)) }
            }
        }
    }
}

@Composable
internal fun NoteThumbnail(image: NoteContentBlock, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = modifier.size(62.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = SurfaceWarm,
        shape = RoundedCornerShape(6.dp),
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = image.altText ?: "笔记图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun NoteAdaptiveImage(image: NoteContentBlock, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val painter = rememberAsyncImagePainter(image.url)
    val state = painter.state
    var ratio by remember(image.url) { mutableFloatStateOf(4f / 3f) }
    LaunchedEffect(state) {
        val drawable = (state as? AsyncImagePainter.State.Success)?.result?.drawable
        val width = drawable?.intrinsicWidth ?: 0
        val height = drawable?.intrinsicHeight ?: 0
        if (width > 0 && height > 0) ratio = (width.toFloat() / height.toFloat()).coerceIn(0.15f, 3f)
    }
    Surface(
        modifier = modifier.fillMaxWidth().aspectRatio(ratio).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = SurfaceWarm,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = image.altText ?: "笔记图片",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            when (state) {
                is AsyncImagePainter.State.Loading -> CircularProgressIndicator(color = Brand)
                is AsyncImagePainter.State.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BrokenImage, null, tint = InkMuted)
                    Text("图片加载失败", color = InkMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
                else -> Unit
            }
        }
    }
}

@Composable
internal fun StatsPanel(noteCount: Int, bookCount: Int, tagCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp),
        color = HeroSurface,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("吾记统计", color = HeroMuted, fontSize = 12.sp)
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                HeroMetric(noteCount.toString(), "篇笔记")
                HeroMetric(bookCount.toString(), "本书")
                HeroMetric(tagCount.toString(), "个标签")
            }
        }
    }
}

@Composable
internal fun BookCard(book: Book, noteCount: Int, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 40.dp, height = 52.dp).background(BrandSoft, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoStories, null, tint = BrandDeep)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(book.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("${book.author ?: "未知作者"}  ·  $noteCount 条笔记", color = InkMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = InkMuted)
        }
    }
}

@Composable
internal fun SettingRow(title: String, subtitle: String? = null, danger: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (danger) Danger else Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            subtitle?.let { Text(it, color = InkMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp)) }
        }
        Icon(Icons.Default.ChevronRight, null, tint = if (danger) Danger else OutlineSoft)
    }
}

@Composable
internal fun BijiChip(text: String, selected: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) BrandSoft else SurfaceWarm,
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text, color = if (selected) BrandDeep else InkMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@Composable
internal fun TodayPromptCard(day: String, weekday: String, prompt: RandomPrompt?, onClick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, color = Ink, fontSize = 52.sp, fontWeight = FontWeight.Light, lineHeight = 52.sp)
        Text(weekday, color = BrandDeep, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(1.dp).background(OutlineSoft))
        Text(
            prompt?.text ?: "阅读不是把文字带走，而是把某一刻的自己留下。",
            color = InkMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).then(
                if (prompt != null) Modifier.clickable { onClick(prompt.noteId) } else Modifier
            ),
        )
    }
}

private val NOTE_DATE = DateTimeFormatter.ofPattern("M月d日 HH:mm").withZone(ZoneId.systemDefault())

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.biji.android.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Highlight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.LibraryExport
import com.personal.biji.android.domain.LibraryRepository
import com.personal.biji.android.domain.LibraryState
import com.personal.biji.android.domain.Note
import com.personal.biji.android.domain.NoteContentBlock
import com.personal.biji.android.domain.SYSTEM_TAG_NAMES
import com.personal.biji.android.domain.Tag
import com.personal.biji.android.domain.StorageMode
import com.personal.biji.android.domain.bookNoteStats
import com.personal.biji.android.domain.isSystemTagName
import com.personal.biji.android.domain.tagsByNote
import com.personal.biji.android.editor.EditorBlock
import com.personal.biji.android.editor.SpannableEditor
import com.personal.biji.android.editor.TextFormat
import com.personal.biji.android.editor.blocksFromDocument
import com.personal.biji.android.editor.contentBlocksFromEditorBlocks
import com.personal.biji.android.editor.documentFromEditorBlocks
import com.personal.biji.android.editor.insertImageBlock
import com.personal.biji.android.editor.editorTextBlockHeightDp
import com.personal.biji.android.editor.plainDocument
import com.personal.biji.android.editor.summary
import com.personal.biji.android.editor.textFromEditorBlocks
import com.personal.biji.android.upload.ImageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class Tab(val title: String) { Recent("最近"), Books("书籍"), Search("搜索"), My("我的") }

@Composable
fun BijiApp(
    repository: LibraryRepository,
    imageUploader: ImageUploader,
    launchRequests: Flow<AppLaunchRequest> = emptyFlow(),
    ocrRecognizer: OcrTextRecognizer? = null,
) {
    val state by repository.state.collectAsState()
    val context = LocalContext.current
    val fontPreferenceStore = remember(context) { FontPreferenceStore(context.applicationContext) }
    val fontPreference by fontPreferenceStore.preference.collectAsState()
    val themePreferenceStore = remember(context) { ThemePreferenceStore(context.applicationContext) }
    val themePreference by themePreferenceStore.preference.collectAsState()
    var tab by remember { mutableStateOf(Tab.Recent) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var backStack by remember { mutableStateOf<List<Screen>>(emptyList()) }
    var previewImage by remember { mutableStateOf<NoteContentBlock?>(null) }
    var pendingDeletion by remember { mutableStateOf<DeletionRequest?>(null) }
    var quickCreateCandidate by remember { mutableStateOf<QuickCreateCandidate?>(null) }
    var pendingEditorQuickCreate by remember { mutableStateOf<QuickCreateCandidate?>(null) }
    var pendingEditorOcrBlocks by remember { mutableStateOf<List<EditorBlock>?>(null) }
    var quickCreateError by remember { mutableStateOf<String?>(null) }
    var ocrCapture by remember { mutableStateOf<OcrCaptureState?>(null) }
    var scanMenuTarget by remember { mutableStateOf<OcrTarget?>(null) }
    var scanPickerTarget by remember { mutableStateOf<OcrTarget?>(null) }
    var scanCameraUri by remember { mutableStateOf<Uri?>(null) }
    var suppressQuickScanUntil by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val recentListState = rememberLazyListState()
    val booksListState = rememberLazyListState()
    LaunchedEffect(Unit) { repository.refresh() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val quickCreateStore = remember(context) { QuickCreateStore(context.applicationContext) }
    val recentBookStore = remember(context) { SharedPreferencesRecentBookStore(context.applicationContext) }
    val photoPermission = remember { photoReadPermission() }
    suspend fun scanQuickCreate() {
        delay(200)
        if (quickCreateCandidate != null || ocrCapture != null || System.currentTimeMillis() < suppressQuickScanUntil) return
        val candidates = recentQuickCreateCandidates(context)
        val selected = selectQuickCreateCandidate(candidates, quickCreateStore.processedFingerprints(), System.currentTimeMillis())
        if (selected != null) {
            quickCreateStore.consume(candidates.filterNot { it.fingerprint == selected.fingerprint })
            quickCreateCandidate = selected
            quickCreateError = null
        }
    }
    val photoPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { scanQuickCreate() }
    }
    fun startOcr(uri: Uri, target: OcrTarget) {
        ocrCapture = OcrCaptureState(uri = uri, target = target)
    }
    val scanGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val target = scanPickerTarget
        scanPickerTarget = null
        if (uri != null && target != null) startOcr(uri, target)
    }
    val scanCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = scanCameraUri
        val target = scanPickerTarget
        scanPickerTarget = null
        if (ok && uri != null && target != null) startOcr(uri, target)
    }
    fun launchScanCamera(target: OcrTarget) {
        scanPickerTarget = target
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val directory = File(context.cacheDir, "camera").apply { mkdirs() }
            scanCameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(directory, "ocr-${System.currentTimeMillis()}.jpg"))
            scanCameraUri?.let(scanCamera::launch)
        }
    }
    val scanCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val target = scanPickerTarget
        if (granted && target != null) launchScanCamera(target) else scanPickerTarget = null
    }
    fun requestScanCamera(target: OcrTarget) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchScanCamera(target)
        else {
            scanPickerTarget = target
            scanCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    fun requestScanGallery(target: OcrTarget) {
        scanPickerTarget = target
        scanGallery.launch("image/*")
    }
    fun navigateTo(target: Screen) {
        if (screen != target) {
            backStack = backStack + screen
            screen = target
        }
    }
    fun goHome() {
        backStack = emptyList()
        screen = Screen.Home
    }
    fun goBack() {
        if (previewImage != null) {
            previewImage = null
            return
        }
        val previous = backStack.lastOrNull()
        if (previous == null) {
            goHome()
        } else {
            backStack = backStack.dropLast(1)
            screen = previous
        }
    }
    LaunchedEffect(ocrCapture?.uri, ocrCapture?.attempt) {
        val capture = ocrCapture ?: return@LaunchedEffect
        val recognizer = ocrRecognizer
        if (recognizer == null) {
            ocrCapture = capture.copy(loading = false, error = "OCR 识别器不可用")
            return@LaunchedEffect
        }
        runCatching { recognizer.recognize(capture.uri) }
            .onSuccess { paragraphs -> ocrCapture = capture.copy(paragraphs = paragraphs, loading = false) }
            .onFailure { error -> ocrCapture = capture.copy(loading = false, error = error.message ?: "图片识别失败，请重试") }
    }
    LaunchedEffect(launchRequests) {
        launchRequests.collect { request ->
            suppressQuickScanUntil = System.currentTimeMillis() + 1_500
            when (request) {
                AppLaunchRequest.NewTextNote -> navigateTo(Screen.Editor(null))
                AppLaunchRequest.ScanFromCamera -> requestScanCamera(OcrTarget.NewNote)
                AppLaunchRequest.ScanFromGallery -> requestScanGallery(OcrTarget.NewNote)
                is AppLaunchRequest.SharedText -> {
                    quickCreateCandidate = textQuickCreateCandidate(request.value, System.currentTimeMillis())
                    quickCreateError = null
                }
                is AppLaunchRequest.SharedImage -> startOcr(request.uri, OcrTarget.NewNote)
            }
        }
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, photoPermission) == PackageManager.PERMISSION_GRANTED) {
            scanQuickCreate()
        } else if (!quickCreateStore.hasAskedForPhotoPermission()) {
            quickCreateStore.markPhotoPermissionAsked()
            photoPermissionLauncher.launch(photoPermission)
        } else {
            scanQuickCreate()
        }
    }
    DisposableEffect(lifecycleOwner, quickCreateCandidate) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) scope.launch { scanQuickCreate() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = previewImage != null || screen != Screen.Home) {
        goBack()
    }
    BijiTheme(fontPreference, themePreference) {
        when (val current = screen) {
            Screen.Home -> Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Surface, tonalElevation = 2.dp) {
                        Tab.entries.forEach {
                            NavigationBarItem(selected = tab == it, onClick = { tab = it }, icon = {
                                Icon(when (it) { Tab.Recent -> Icons.AutoMirrored.Filled.Note; Tab.Books -> Icons.Default.Book; Tab.Search -> Icons.Default.Search; Tab.My -> Icons.Default.Person }, null)
                            }, label = { Text(it.title) }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandSoft, selectedIconColor = BrandDeep, selectedTextColor = BrandDeep))
                        }
                    }
                },
                floatingActionButton = {
                    if (tab == Tab.Recent || tab == Tab.Books) FloatingActionButton(onClick = { navigateTo(Screen.Editor(null)) }, containerColor = Brand, contentColor = Color.White, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "新建笔记") }
                },
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize().background(Canvas)) {
                    when (tab) {
                        Tab.Recent -> RecentScreen(state, recentListState) { navigateTo(Screen.NoteDetail(it)) }
                        Tab.Books -> BooksScreen(state, booksListState, { navigateTo(Screen.BookDetail(it)) }, { title, author -> scope.launch { repository.createBook(title, author) } })
                        Tab.Search -> SearchScreen(repository, state) { navigateTo(Screen.NoteDetail(it)) }
                        Tab.My -> MyScreen(repository, state, fontPreference, fontPreferenceStore::setPreference, themePreference, themePreferenceStore::setPreference, { navigateTo(Screen.Tags) }, { navigateTo(Screen.Export(it.first, it.second)) }) {
                            pendingDeletion = DeletionRequest("清空本地数据？", "这会删除当前模式下保存在这台设备的数据。") {
                                scope.launch { repository.clearLocal() }
                            }
                        }
                    }
                }
            }
            is Screen.NoteDetail -> {
                val note = state.notes.firstOrNull { it.id == current.note.id } ?: current.note
                NoteDetailScreen(note, state, ::goBack, { navigateTo(Screen.Editor(note)) }, { previewImage = it }) {
                    pendingDeletion = DeletionRequest("删除这条笔记？", "删除后会从当前存储模式中移除这条笔记。") {
                        scope.launch { repository.deleteNote(note.id); goBack() }
                    }
                }
            }
            is Screen.BookDetail -> BookDetailScreen(current.book, state, ::goBack, { navigateTo(Screen.NoteDetail(it)) }, { navigateTo(Screen.Editor(null, current.book.id)) }, {
                navigateTo(Screen.Export("Markdown 导出", LibraryExport.markdownForBook(current.book, state.notes, tagsByNote(state.tags, state.noteTags))))
            }) {
                pendingDeletion = DeletionRequest("删除这本书？", "删除书籍会同时移除它下面的笔记。") {
                    scope.launch { repository.deleteBook(current.book.id); goHome() }
                }
            }
            is Screen.Editor -> NoteEditorScreen(
                current.note,
                current.bookId,
                current.quickCreateSeed,
                current.initialBlocks,
                pendingEditorQuickCreate,
                pendingEditorOcrBlocks,
                fontPreference,
                state,
                repository,
                imageUploader,
                recentBookStore,
                { previewImage = it },
                {
                    quickCreateStore.consume(listOf(it))
                    quickCreateCandidate = null
                    pendingEditorQuickCreate = null
                    quickCreateError = null
                    navigateTo(Screen.Editor(null, quickCreateSeed = it.seed))
                },
                {
                    pendingEditorQuickCreate = null
                    quickCreateError = it
                },
                { pendingEditorOcrBlocks = null },
                { scanMenuTarget = OcrTarget.CurrentEditor },
            ) { goBack() }
            Screen.Tags -> TagsScreen(state, repository, ::goBack) { tag ->
                pendingDeletion = DeletionRequest("删除这个标签？", "删除后这枚标签会从相关笔记上移除。") {
                    scope.launch { repository.deleteTag(tag.id) }
                }
            }
            is Screen.Export -> TextPage(current.title, current.text, ::goBack)
        }
        pendingDeletion?.let { request ->
            DeleteConfirmationDialog(request, { pendingDeletion = null }) {
                val action = request.confirm
                pendingDeletion = null
                action()
            }
        }
        previewImage?.let { image ->
            ImagePreviewDialog(image) { previewImage = null }
        }
        quickCreateCandidate?.let { candidate ->
            QuickCreateDialog(candidate.seed, quickCreateError, {
                quickCreateStore.consume(listOf(candidate))
                quickCreateCandidate = null
                pendingEditorQuickCreate = null
                quickCreateError = null
            }) {
                if (screen is Screen.Editor) {
                    pendingEditorQuickCreate = candidate
                    quickCreateError = null
                } else {
                    quickCreateStore.consume(listOf(candidate))
                    quickCreateCandidate = null
                    quickCreateError = null
                    navigateTo(Screen.Editor(null, quickCreateSeed = candidate.seed))
                }
            }
        }
        scanMenuTarget?.let { target ->
            ScanSourceSheet(
                dismiss = { scanMenuTarget = null },
                camera = {
                    scanMenuTarget = null
                    requestScanCamera(target)
                },
                gallery = {
                    scanMenuTarget = null
                    requestScanGallery(target)
                },
            )
        }
        ocrCapture?.let { capture ->
            OcrCaptureDialog(
                capture = capture,
                dismiss = { ocrCapture = null },
                toggleParagraph = { id ->
                    ocrCapture = capture.copy(paragraphs = capture.paragraphs.map { if (it.id == id) it.copy(selected = !it.selected) else it })
                },
                toggleKeepImage = { ocrCapture = capture.copy(keepImage = it) },
                retry = { ocrCapture = capture.copy(loading = true, error = null, attempt = capture.attempt + 1) },
            ) {
                scope.launch {
                    val text = selectedOcrText(capture.paragraphs)
                    var image: NoteContentBlock? = null
                    if (capture.keepImage) {
                        val upload = runCatching {
                            withContext(Dispatchers.IO) {
                                val mimeType = mimeTypeForUri(context, capture.uri)
                                val bytes = context.contentResolver.openInputStream(capture.uri)?.use { it.readBytes() } ?: error("无法读取图片")
                                imageUploader.upload(bytes, mimeType)
                            }
                        }
                        if (upload.isFailure) {
                            ocrCapture = capture.copy(error = upload.exceptionOrNull()?.message ?: "图片上传失败，请重试")
                            return@launch
                        }
                        image = upload.getOrThrow().let { NoteContentBlock(type = "image", url = it.url, objectKey = it.objectKey, altText = "摘抄原图") }
                    }
                    val blocks = ocrEditorBlocks(text, image)
                    ocrCapture = null
                    if (capture.target == OcrTarget.CurrentEditor && screen is Screen.Editor) pendingEditorOcrBlocks = blocks
                    else navigateTo(Screen.Editor(null, initialBlocks = blocks))
                }
            }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data class NoteDetail(val note: Note) : Screen
    data class BookDetail(val book: Book) : Screen
    data class Editor(
        val note: Note?,
        val bookId: String? = null,
        val quickCreateSeed: QuickCreateSeed? = null,
        val initialBlocks: List<EditorBlock>? = null,
    ) : Screen
    data object Tags : Screen
    data class Export(val title: String, val text: String) : Screen
}

private enum class OcrTarget { NewNote, CurrentEditor }

private data class OcrCaptureState(
    val uri: Uri,
    val target: OcrTarget,
    val paragraphs: List<OcrParagraph> = emptyList(),
    val keepImage: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
    val attempt: Int = 0,
)

private data class DeletionRequest(val title: String, val message: String, val confirm: () -> Unit)

@Composable private fun RecentScreen(state: LibraryState, listState: LazyListState, open: (Note) -> Unit) = Column(Modifier.fillMaxSize()) {
    PageHeader("READING NOTES", "最近", "按时间回到那些真正想留住的片刻。")
    LazyColumn(Modifier.weight(1f).testTag("recent_list"), state = listState) {
        item {
            val now = remember { java.time.LocalDate.now() }
            val prompt = remember(state.notes) { randomPromptFromNotes(state.notes) }
            TodayPromptCard(
                day = now.dayOfMonth.toString().padStart(2, '0'),
                weekday = "${now.monthValue}月｜${CHINESE_WEEKDAYS[now.dayOfWeek.value - 1]}",
                prompt = prompt,
            ) { noteId -> state.notes.firstOrNull { it.id == noteId }?.let(open) }
        }
        if (state.notes.isEmpty()) item { Empty("还没有笔记", "先写下一条读书时真正想留住的想法。") }
        else {
            val notes = state.notes.sortedByDescending { it.updatedAt }
            var previousMonth: String? = null
            notes.forEach { note ->
                val month = noteMonthTitle(note.updatedAt)
                if (month != previousMonth) {
                    item { MonthDivider(month) }
                    previousMonth = month
                }
                item { TimelineNoteCard(note, state.books.firstOrNull { it.id == note.bookId }) { open(note) } }
            }
        }
    }
}

@Composable private fun BooksScreen(state: LibraryState, listState: LazyListState, open: (Book) -> Unit, create: (String, String?) -> Unit) {
    var adding by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        PageHeader("LIBRARY", "书籍", "一页一页，搭起你的私人书架。") { IconButton(onClick = { adding = true }) { Icon(Icons.Default.Add, "添加书籍", tint = BrandDeep) } }
        LazyColumn(Modifier.weight(1f).testTag("books_list"), state = listState) {
        item { SectionLabel("我的书架", if (state.books.isEmpty()) null else "${state.books.size} 本") }
        if (state.books.isEmpty()) item { Empty("还没有书籍", "先添加一本书，再把笔记稳稳地放进去。") }
        else items(state.books) { book -> BookCard(book, state.notes.count { it.bookId == book.id }) { open(book) } }
        }
    }
    if (adding) TextInputDialog("添加书籍", "书名", true, { adding = false }) { title, author -> create(title, author); adding = false }
}

@Composable private fun SearchScreen(repository: LibraryRepository, state: LibraryState, open: (Note) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = if (query.isBlank()) state.notes.sortedByDescending { it.updatedAt } else repository.search(query)
    Column(Modifier.fillMaxSize()) {
        PageHeader("DISCOVER", "搜索", "从书名、作者、正文和标签里找回灵感。")
        OutlinedTextField(query, { query = it }, label = { Text("书名、作者、正文或标签", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp))
        SectionLabel(if (query.isBlank()) "全部笔记" else "搜索结果", "${results.size} 篇")
        LazyColumn(Modifier.weight(1f)) { NoteList(results, state, open) }
    }
}

@Composable private fun MyScreen(
    repository: LibraryRepository,
    state: LibraryState,
    fontPreference: FontPreference,
    setFontPreference: (FontPreference) -> Unit,
    themePreference: ThemePreference,
    setThemePreference: (ThemePreference) -> Unit,
    openTags: () -> Unit,
    openExport: (Pair<String, String>) -> Unit,
    clearLocal: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        PageHeader("PROFILE", "我的", "数据、设置与个人阅读笔记状态。")
        LazyColumn(Modifier.weight(1f)) {
        item { StatsPanel(state.notes.size, state.books.size, state.tags.size) }
        item { SectionLabel("存储模式") }
        item {
            SettingsGroup {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StorageMode.entries.forEach { mode ->
                        BijiChip(mode.title, selected = state.storageMode == mode) {
                            scope.launch { repository.setStorageMode(mode) }
                        }
                    }
                }
                Text(
                    if (state.storageMode == StorageMode.Local) "当前数据只保存在这台设备，图片也写入本机私有目录。"
                    else "当前数据来自远端，新增和编辑会写入远端后再更新本机缓存。",
                    color = InkMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        item { SectionLabel("整理") }
        item { SettingsGroup {
            SettingRow("标签管理", "${state.tags.size} 个标签") { openTags() }
        } }
        item { SectionLabel("外观") }
        item {
            SettingsGroup {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePreference.entries.forEach { preference ->
                        BijiChip(preference.title, selected = themePreference == preference) {
                            setThemePreference(preference)
                        }
                    }
                }
            }
        }
        item { SectionLabel("阅读字体") }
        item {
            SettingsGroup {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontPreference.entries.forEach { preference ->
                        BijiChip(preference.title, selected = fontPreference == preference) {
                            setFontPreference(preference)
                        }
                    }
                }
            }
        }
        item { SectionLabel("导出与备份") }
        item { SettingsGroup {
            SettingRow("导出 Markdown", "适合迁移到其他笔记工具") { openExport("Markdown 导出" to repository.markdown()) }
            SettingRow("导出 JSON", "保留完整结构化备份") { openExport("JSON 备份" to repository.jsonBackup()) }
            SettingRow("分享 Markdown", "通过系统分享菜单发送") {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, repository.markdown()) }, "分享导出"))
            }
        } }
        item { SectionLabel("关于吾记") }
        item { SettingsGroup {
            SettingRow("隐私政策", "可在本地存储与远端模式之间切换") {}
            SettingRow("用户协议", "用于个人阅读记录与资料整理") {}
        } }
        item { SectionLabel("设备数据") }
        item { SettingsGroup { SettingRow("清空本地数据", "删除这台设备上的本地内容", danger = true) { clearLocal() } } }
        item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable private fun NoteDetailScreen(note: Note, state: LibraryState, back: () -> Unit, edit: () -> Unit, preview: (NoteContentBlock) -> Unit, delete: () -> Unit) {
    val book = state.books.firstOrNull { it.id == note.bookId }
    var showingTimeline by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Canvas)) {
        AppTopBar("笔记详情", back, actions = {
            IconButton(onClick = edit) { Icon(Icons.Default.Edit, "编辑") }; IconButton(onClick = delete) { Icon(Icons.Default.Delete, "删除", tint = Danger) }
        })
        LazyColumn {
          item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(book?.title ?: "未命名书籍", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.padding(top = 7.dp).clickable { showingTimeline = true }, verticalAlignment = Alignment.CenterVertically) {
                    Text("更新于 ${DISPLAY.format(note.updatedAt)}", color = InkMuted, fontSize = 11.sp)
                    Text("  查看时间线", color = BrandDeep, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                val summary = noteDisplaySummary(note)
                if (summary.isNotBlank()) {
                    Text(summary, color = Ink, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 18.dp))
                }
                noteImages(note).forEach { block ->
                    NoteAdaptiveImage(block, Modifier.padding(top = 4.dp)) { preview(block) }
                }
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (note.isFavorite) BijiChip("已收藏", selected = true) {}
                    if (note.isArchived) BijiChip("已归档") {}
                    state.noteTags.filter { it.noteId == note.id }.mapNotNull { link -> state.tags.firstOrNull { it.id == link.tagId } }.forEach { tag ->
                        BijiChip(tag.name, selected = true) {}
                    }
                }
            }
          }
        }
    }
    if (showingTimeline) {
        NoteTimelineSheet(note) { showingTimeline = false }
    }
}

@Composable private fun BookDetailScreen(book: Book, state: LibraryState, back: () -> Unit, open: (Note) -> Unit, create: () -> Unit, exportMarkdown: () -> Unit, delete: () -> Unit) {
    val allNotes = state.notes.filter { it.bookId == book.id }.sortedByDescending { it.updatedAt }
    val stats = bookNoteStats(book, state.notes, state.tags, state.noteTags)
    val tagCounts = bookTagCounts(book, state)
    val visibleFilters = visibleBookFilters(tagCounts)
    var selectedTagId by remember(book.id) { mutableStateOf<String?>(null) }
    var showingFilterSheet by remember { mutableStateOf(false) }
    var expandedNoteTags by remember { mutableStateOf<List<Tag>?>(null) }
    val notes = selectedTagId?.let { tagId -> allNotes.filter { note -> state.noteTags.any { it.noteId == note.id && it.tagId == tagId } } } ?: allNotes
    Column(Modifier.fillMaxSize().background(Canvas)) {
        AppTopBar("书籍详情", back, actions = {
            IconButton(onClick = exportMarkdown) { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, "导出 Markdown", tint = BrandDeep) }
            IconButton(onClick = create) { Icon(Icons.Default.Add, "新建笔记", tint = BrandDeep) }
            IconButton(onClick = delete) { Icon(Icons.Default.Delete, "删除书籍", tint = Danger) }
        })
        LazyColumn {
            item {
                BookStatsHeader(book, stats)
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BijiChip("全部 ${stats.totalCount}", selected = selectedTagId == null) { selectedTagId = null }
                    visibleFilters.forEach { tagCount ->
                        BijiChip("${tagCount.tag.name} ${tagCount.count}", selected = selectedTagId == tagCount.tag.id) {
                            selectedTagId = if (selectedTagId == tagCount.tag.id) null else tagCount.tag.id
                        }
                    }
                    val hidden = tagCounts.size - visibleFilters.size
                    if (hidden > 0) BijiChip("+$hidden", selected = false) { showingFilterSheet = true }
                }
            }
            item { SectionLabel(if (selectedTagId == null) "书中笔记" else "筛选结果", "${notes.size} 篇") }
            NoteList(notes, state, open) { expandedNoteTags = it }
        }
    }
    if (showingFilterSheet) {
        TagListSheet("筛选标签", tagCounts, selectedTagId, { showingFilterSheet = false }) { tag ->
            selectedTagId = if (selectedTagId == tag.id) null else tag.id
            showingFilterSheet = false
        }
    }
    expandedNoteTags?.let { tags ->
        TagListSheet("全部标签", tags.map { TagCount(it, 0) }, null, { expandedNoteTags = null }) { expandedNoteTags = null }
    }
}

@Composable private fun TagsScreen(state: LibraryState, repository: LibraryRepository, back: () -> Unit, requestDelete: (Tag) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    Column(Modifier.fillMaxSize().background(Canvas)) {
        AppTopBar("标签管理", back)
        CompactPageHeader("标签", "用轻量分类整理想法")
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("新标签名称", fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
            IconButton(onClick = { scope.launch { repository.createTag(name); name = "" } }) { Icon(Icons.Default.Add, "添加标签") }
        }
        LazyColumn(Modifier.testTag("tags_list")) { items(orderedTags(state.tags)) { tag -> Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            CardRow("${tag.name}  ·  ${state.noteTags.count { it.tagId == tag.id }} 条笔记", Modifier.weight(1f)) { selectedTag = tag }
            if (isSystemTagName(tag.name)) Text("系统标签", color = BrandDeep, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp))
            else IconButton(onClick = { requestDelete(tag) }) { Icon(Icons.Default.Delete, "删除", tint = Danger) }
        } } }
        selectedTag?.let { tag ->
            Text("${tag.name} 下的笔记", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(18.dp))
            LazyColumn { NoteList(state.notes.filter { note -> state.noteTags.any { it.noteId == note.id && it.tagId == tag.id } }, state, {}) }
        }
    }
}

@Composable private fun NoteEditorScreen(
    note: Note?,
    preselectedBookId: String?,
    quickCreateSeed: QuickCreateSeed?,
    initialEditorBlocks: List<EditorBlock>?,
    requestedQuickCreate: QuickCreateCandidate?,
    requestedOcrBlocks: List<EditorBlock>?,
    fontPreference: FontPreference,
    state: LibraryState,
    repository: LibraryRepository,
    uploader: ImageUploader,
    recentBookStore: RecentBookStore,
    preview: (NoteContentBlock) -> Unit,
    switchToQuickCreate: (QuickCreateCandidate) -> Unit,
    quickCreateFailed: (String) -> Unit,
    ocrBlocksApplied: () -> Unit,
    openOcr: () -> Unit,
    close: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val defaultBookId = resolveDefaultBookId(state.books, recentBookStore.get(state.storageMode))
    var bookId by remember { mutableStateOf(note?.bookId ?: preselectedBookId ?: defaultBookId) }
    val draftNote = remember(note?.id) { note ?: Note(bookId = preselectedBookId ?: defaultBookId.orEmpty(), content = "") }
    var savedNote by remember(note?.id) { mutableStateOf(note) }
    var isSaving by remember { mutableStateOf(false) }
    var editorBlocks by remember(note?.id, quickCreateSeed, initialEditorBlocks) {
        mutableStateOf(initialEditorBlocks ?: seedEditorBlocks(quickCreateSeed) ?: blocksFromDocument(note?.richTextDocument, note?.content.orEmpty(), note?.contentBlocks.orEmpty()))
    }
    val text = textFromEditorBlocks(editorBlocks)
    val imageBlocks = contentBlocksFromEditorBlocks(editorBlocks)
    val document = documentFromEditorBlocks(editorBlocks)
    val hasMediaBlocks = imageBlocks.isNotEmpty()
    var favorite by remember { mutableStateOf(note?.isFavorite ?: false) }
    var archived by remember { mutableStateOf(note?.isArchived ?: false) }
    var selectedTags by remember { mutableStateOf(state.noteTags.filter { it.noteId == note?.id }.map { it.tagId }.toSet()) }
    var format by remember { mutableStateOf<TextFormat?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var recentPhoto by remember { mutableStateOf<Uri?>(null) }
    var addingBook by remember { mutableStateOf(false) }
    var addingTag by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { recentPhoto = latestRecentPhoto(context) }
    fun upload(uri: Uri) {
        scope.launch {
            uploading = true
            uploadError = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val mimeType = mimeTypeForUri(context, uri)
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("无法读取图片")
                    uploader.upload(bytes, mimeType)
                }
            }.onSuccess {
                val block = NoteContentBlock(type = "image", url = it.url, objectKey = it.objectKey, altText = "笔记图片")
                editorBlocks = insertImageBlock(editorBlocks, block)
            }.onFailure {
                uploadError = it.message ?: "图片插入失败，请重试"
            }
            uploading = false
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(::upload) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) cameraUri?.let(::upload) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            runCatching {
                val directory = File(context.cacheDir, "camera").apply { mkdirs() }
                cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(directory, "note-${System.currentTimeMillis()}.jpg"))
                cameraUri?.let(camera::launch)
            }.onFailure { uploadError = it.message ?: "无法打开相机" }
        } else {
            uploadError = "需要相机权限才能拍照插入"
        }
    }
    suspend fun save(): Boolean {
        val targetBookId = bookId
        if (text.isBlank() && imageBlocks.isEmpty()) return true
        if (uploading) return false
        if (isSaving) return false
        if (targetBookId == null) return false
        isSaving = true
        val result = runCatching {
            val base = savedNote ?: draftNote
            repository.saveNote(
                base.copy(
                    bookId = targetBookId,
                    content = text.ifBlank { if (imageBlocks.isNotEmpty()) "[图片]" else text },
                    contentBlocks = imageBlocks,
                    richTextDocument = document,
                    isFavorite = favorite,
                    isArchived = archived,
                ),
                selectedTags.toList(),
            )
        }.onSuccess {
            savedNote = it
            recentBookStore.set(state.storageMode, targetBookId)
        }
        isSaving = false
        return result.isSuccess
    }
    LaunchedEffect(editorBlocks, favorite, archived, selectedTags, bookId) { delay(5_000); if ((text.isNotBlank() || imageBlocks.isNotEmpty()) && bookId != null) save() }
    LaunchedEffect(requestedQuickCreate?.fingerprint) {
        val candidate = requestedQuickCreate ?: return@LaunchedEffect
        if (save()) switchToQuickCreate(candidate)
        else quickCreateFailed("当前笔记保存失败，请检查书籍选择或稍后重试。")
    }
    LaunchedEffect(requestedOcrBlocks) {
        val blocks = requestedOcrBlocks ?: return@LaunchedEffect
        editorBlocks = appendOcrEditorBlocks(editorBlocks, blocks)
        ocrBlocksApplied()
    }
    Scaffold(containerColor = Canvas, topBar = {
        Row(
            Modifier.fillMaxWidth().background(Canvas).statusBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = close, contentPadding = PaddingValues(horizontal = 0.dp)) { Text("返回", color = BrandDeep, fontSize = 14.sp) }
            Text(if (note == null) "新建笔记" else "编辑笔记", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            TextButton(onClick = { scope.launch { if (save()) close() } }, enabled = (text.isNotBlank() || imageBlocks.isNotEmpty()) && bookId != null && !uploading && !isSaving, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(if (isSaving) "保存中" else "保存", color = BrandDeep, fontSize = 14.sp) }
        }
    }, bottomBar = {
        BottomAppBar(containerColor = Surface, tonalElevation = 1.dp, actions = {
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                orderedTags(state.tags).forEach { tag -> BijiChip(tag.name, tag.id in selectedTags) { selectedTags = if (tag.id in selectedTags) selectedTags - tag.id else selectedTags + tag.id } }
                if (state.tags.isEmpty()) Text("暂无标签", color = InkMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            IconButton(onClick = { addingTag = true }) { Icon(Icons.Default.Add, "新建标签") }
            IconButton(onClick = { favorite = !favorite }) { Icon(if (favorite) Icons.Default.Star else Icons.Outlined.Star, "收藏", tint = if (favorite) BrandDeep else InkMuted) }
            IconButton(onClick = { archived = !archived }) { Icon(if (archived) Icons.Default.Unarchive else Icons.Default.Archive, "归档", tint = if (archived) BrandDeep else InkMuted) }
            Text("${text.trim().length}", color = InkMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
        })
    }) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("记录到", color = InkMuted, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.books.forEach { book -> BijiChip(book.title, bookId == book.id) { bookId = book.id } }
                    if (state.books.isEmpty()) Text("先新建一本书", color = InkMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
                IconButton(onClick = { addingBook = true }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Add, "新建书籍") }
            }
            Surface(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 18.dp, vertical = 6.dp), color = Surface, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        EditorToolButton("拍照插入", Icons.Outlined.CameraAlt) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                runCatching {
                                    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
                                    cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(directory, "note-${System.currentTimeMillis()}.jpg"))
                                    cameraUri?.let(camera::launch)
                                }.onFailure { uploadError = it.message ?: "无法打开相机" }
                            } else {
                                cameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        }
                        EditorToolButton("从相册插入", Icons.Outlined.Image) { photoPicker.launch("image/*") }
                        EditorToolButton("扫描摘抄", Icons.Outlined.DocumentScanner, openOcr)
                        EditorToolButton("加粗", Icons.Outlined.FormatBold) { format = TextFormat.Bold }
                        EditorToolButton("斜体", Icons.Outlined.FormatItalic) { format = TextFormat.Italic }
                        EditorToolButton("下划线", Icons.Outlined.FormatUnderlined) { format = TextFormat.Underline }
                        EditorToolButton("标题", Icons.Outlined.Title) { format = TextFormat.Heading }
                        EditorToolButton("引用", Icons.Outlined.FormatQuote) { format = TextFormat.Quote }
                        EditorToolButton("项目符号", Icons.AutoMirrored.Outlined.FormatListBulleted) { format = TextFormat.Bullet }
                        EditorToolButton("编号列表", Icons.Outlined.FormatListNumbered) { format = TextFormat.Numbered }
                        EditorToolButton("链接", Icons.Outlined.Link) { format = TextFormat.Link }
                        EditorToolButton("高亮", Icons.Outlined.Highlight) { format = TextFormat.Highlight }
                    }
                    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                        editorBlocks.forEach { block ->
                            when (block) {
                                is EditorBlock.Text -> SpannableEditor(
                                    plainDocument(block.text),
                                    format,
                                    fontPreference,
                                    Ink.toArgb(),
                                    InkMuted.toArgb(),
                                    Modifier.fillMaxWidth().height(editorTextBlockHeightDp(block.text, hasMediaBlocks).dp),
                                    { format = null },
                                    { changed ->
                                        editorBlocks = editorBlocks.map {
                                            if (it.id == block.id) EditorBlock.Text(block.id, summary(changed)) else it
                                        }
                                    },
                                )
                                is EditorBlock.Image -> NoteAdaptiveImage(block.image) { preview(block.image) }
                            }
                        }
                        if (uploading || uploadError != null || imageBlocks.isNotEmpty()) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                                if (uploading) Text("图片上传中...", color = Brand, fontSize = 12.sp)
                                uploadError?.let { Text(it, color = Danger, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
            recentPhoto?.let { uri -> TextButton(onClick = { upload(uri); recentPhoto = null }) { Text("插入刚刚的图片") } }
        }
    }
    if (addingBook) TextInputDialog("新建书籍", "书名", true, { addingBook = false }) { title, author ->
        scope.launch { bookId = repository.createBook(title, author).id }
        addingBook = false
    }
    if (addingTag) TextInputDialog("新建标签", "标签名", false, { addingTag = false }) { name, _ ->
        scope.launch { val tag = repository.createTag(name); selectedTags = selectedTags + tag.id }
        addingTag = false
    }
}

@Composable private fun TextPage(title: String, text: String, back: () -> Unit) = Column {
    AppTopBar(title, back)
    Text(text.ifBlank { "暂无可导出的内容。" }, modifier = Modifier.padding(16.dp))
}

private data class TagCount(val tag: Tag, val count: Int)

@Composable private fun BookStatsHeader(book: Book, stats: com.personal.biji.android.domain.BookNoteStats) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), color = HeroSurface, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(book.title, color = HeroText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(book.author ?: "未知作者", color = HeroMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                BookMetric(stats.quoteCount.toString(), "摘抄")
                BookMetric(stats.thoughtCount.toString(), "想法")
                BookMetric(stats.totalCount.toString(), "全部")
            }
            Text(
                "最后记录 ${stats.lastRecordedAt?.let { DISPLAY.format(it) } ?: "暂无"}",
                color = HeroMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable private fun BookMetric(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, color = Brand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = HeroMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun TagListSheet(title: String, tagCounts: List<TagCount>, selectedTagId: String?, dismiss: () -> Unit, select: (Tag) -> Unit) = ModalBottomSheet(
    onDismissRequest = dismiss,
    containerColor = Canvas,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("${tagCounts.size} 个标签", color = InkMuted, fontSize = 12.sp)
        }
        FlowRow(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tagCounts.forEach { tagCount ->
                val label = if (tagCount.count > 0) "${tagCount.tag.name} ${tagCount.count}" else tagCount.tag.name
                BijiChip(label, selected = selectedTagId == tagCount.tag.id) { select(tagCount.tag) }
            }
        }
    }
}

private fun orderedTags(tags: List<Tag>): List<Tag> =
    tags.sortedWith(compareBy<Tag> { SYSTEM_TAG_NAMES.indexOf(it.name).let { index -> if (index == -1) Int.MAX_VALUE else index } }.thenBy { it.name })

private fun noteTagsFor(note: Note, state: LibraryState): List<Tag> =
    orderedTags(state.noteTags.filter { it.noteId == note.id }.mapNotNull { link -> state.tags.firstOrNull { it.id == link.tagId } })

private fun bookTagCounts(book: Book, state: LibraryState): List<TagCount> {
    val noteIds = state.notes.filter { it.bookId == book.id && it.deletedAt == null }.map { it.id }.toSet()
    return state.noteTags
        .filter { it.noteId in noteIds }
        .groupingBy { it.tagId }
        .eachCount()
        .mapNotNull { (tagId, count) -> state.tags.firstOrNull { it.id == tagId }?.let { TagCount(it, count) } }
        .sortedWith(compareBy<TagCount> { SYSTEM_TAG_NAMES.indexOf(it.tag.name).let { index -> if (index == -1) Int.MAX_VALUE else index } }.thenByDescending { it.count }.thenBy { it.tag.name })
}

private fun visibleBookFilters(tagCounts: List<TagCount>): List<TagCount> {
    val system = tagCounts.filter { isSystemTagName(it.tag.name) }
    val regular = tagCounts.filterNot { isSystemTagName(it.tag.name) }.take(2)
    return system + regular
}

@Composable private fun EditorToolButton(contentDescription: String, icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
        Icon(icon, contentDescription, tint = Ink, modifier = Modifier.size(20.dp))
    }
}

@Composable private fun ListPage(title: String, action: @Composable () -> Unit = {}, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) = Column {
    TopAppBar(title = { Text(title) }, actions = { action() })
    LazyColumn(content = content)
}
private fun androidx.compose.foundation.lazy.LazyListScope.NoteList(notes: List<Note>, state: LibraryState, open: (Note) -> Unit, showAllTags: (List<Tag>) -> Unit = {}) {
    items(notes) { note ->
        val tags = noteTagsFor(note, state)
        NoteCard(note, state.books.firstOrNull { it.id == note.bookId }, tags, showAllTags) { open(note) }
    }
}
@Composable private fun CardRow(text: String, modifier: Modifier = Modifier, click: () -> Unit) = Card(modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(onClick = click), colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(14.dp)) { Text(text, color = Ink, fontSize = 14.sp, modifier = Modifier.padding(14.dp)) }
@Composable private fun Empty(title: String, message: String) = Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) { Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text(message, color = InkMuted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 7.dp)) }
@Composable private fun PageTitle(title: String) = Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

@Composable private fun MonthDivider(title: String) = Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.Center,
) {
    Surface(color = SurfaceWarm, shape = RoundedCornerShape(16.dp)) {
        Text(title, color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
    }
}

@Composable internal fun NoteTimelineSheet(note: Note, dismiss: () -> Unit) = ModalBottomSheet(
    onDismissRequest = dismiss,
    containerColor = Canvas,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Text("时间线", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        TimelineRow("创建", "plus", note.createdAt)
        if (note.updatedAt != note.createdAt) TimelineRow("最后编辑", "edit", note.updatedAt)
    }
}

@Composable private fun TimelineRow(title: String, icon: String, date: java.time.Instant) = Surface(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    color = Surface,
    shape = RoundedCornerShape(16.dp),
) {
    Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Surface(color = BrandSoft, shape = RoundedCornerShape(18.dp)) {
            Icon(if (icon == "plus") Icons.Default.Add else Icons.Default.Edit, null, tint = BrandDeep, modifier = Modifier.padding(8.dp).size(18.dp))
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(DISPLAY.format(date), color = InkMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable private fun DeleteConfirmationDialog(request: DeletionRequest, dismiss: () -> Unit, confirm: () -> Unit) = AlertDialog(
    onDismissRequest = dismiss,
    title = { Text(request.title) },
    text = { Text(request.message) },
    confirmButton = { TextButton(onClick = confirm) { Text("删除", color = Danger) } },
    dismissButton = { TextButton(onClick = dismiss) { Text("取消") } },
)

@Composable private fun ImagePreviewDialog(image: NoteContentBlock, close: () -> Unit) = Dialog(onDismissRequest = close) {
    Surface(color = Color.Black, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = close) { Text("关闭", color = Color.White) }
            }
            NoteAdaptiveImage(image, Modifier.padding(bottom = 12.dp))
        }
    }
}

@Composable private fun SettingsGroup(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) = Surface(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
    color = Surface,
    shape = RoundedCornerShape(16.dp),
) { Column(content = content) }

@Composable private fun AppTopBar(title: String, back: () -> Unit, actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}) = TopAppBar(
    title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
    navigationIcon = { TextButton(onClick = back) { Text("返回", fontSize = 14.sp, color = BrandDeep) } },
    actions = actions,
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Canvas),
)

@Composable private fun TextInputDialog(title: String, label: String, includeAuthor: Boolean, dismiss: () -> Unit, save: (String, String?) -> Unit) {
    var value by remember { mutableStateOf("") }; var author by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = dismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value, { value = it }, label = { Text(label, fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand, focusedLabelColor = BrandDeep))
                if (includeAuthor) OutlinedTextField(author, { author = it }, label = { Text("作者", fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand, focusedLabelColor = BrandDeep))
            }
        },
        confirmButton = {
            Button(onClick = { save(value, author.ifBlank { null }) }, enabled = value.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Brand)) { Text("保存", fontSize = 13.sp) }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("取消", fontSize = 13.sp, color = InkMuted) } },
    )
}

@Composable private fun ScanSourceSheet(dismiss: () -> Unit, camera: () -> Unit, gallery: () -> Unit) = ModalBottomSheet(
    onDismissRequest = dismiss,
    containerColor = Canvas,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("扫描摘抄", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        CardRow("拍照识别", click = camera)
        CardRow("从相册识别", click = gallery)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable private fun OcrCaptureDialog(
    capture: OcrCaptureState,
    dismiss: () -> Unit,
    toggleParagraph: (String) -> Unit,
    toggleKeepImage: (Boolean) -> Unit,
    retry: () -> Unit,
    insert: () -> Unit,
) = AlertDialog(
    onDismissRequest = dismiss,
    containerColor = Surface,
    shape = RoundedCornerShape(20.dp),
    title = { Text("识别摘抄", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
    text = {
        Column(Modifier.fillMaxWidth()) {
            if (capture.loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("正在识别图片...", color = InkMuted, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
                }
            } else if (capture.paragraphs.isEmpty()) {
                Text("未识别到文字。你可以保留原图，或重新选择图片。", color = InkMuted, fontSize = 13.sp, lineHeight = 20.sp)
            } else {
                Column(Modifier.height(280.dp).verticalScroll(rememberScrollState())) {
                    capture.paragraphs.forEach { paragraph ->
                        Row(
                            Modifier.fillMaxWidth().clickable { toggleParagraph(paragraph.id) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Checkbox(paragraph.selected, { toggleParagraph(paragraph.id) })
                            Text(paragraph.text, color = Ink, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("同时保留原图", color = InkMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(capture.keepImage, toggleKeepImage)
            }
            capture.error?.let { Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
    },
    confirmButton = {
        Button(
            onClick = insert,
            enabled = !capture.loading && (capture.keepImage || selectedOcrText(capture.paragraphs).isNotBlank()),
            colors = ButtonDefaults.buttonColors(containerColor = Brand),
        ) { Text("插入笔记", fontSize = 13.sp) }
    },
    dismissButton = {
        Row {
            if (capture.error != null) TextButton(onClick = retry) { Text("重试", color = BrandDeep, fontSize = 13.sp) }
            TextButton(onClick = dismiss) { Text("取消", color = InkMuted, fontSize = 13.sp) }
        }
    },
)

@Composable private fun QuickCreateDialog(seed: QuickCreateSeed, error: String?, dismiss: () -> Unit, create: () -> Unit) = AlertDialog(
    onDismissRequest = dismiss,
    containerColor = Surface,
    shape = RoundedCornerShape(20.dp),
    title = { Text("快速创建笔记", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
    text = {
        Column {
            when (seed) {
                is QuickCreateSeed.Text -> Text(quickCreatePreview(seed), color = Ink, fontSize = 14.sp, lineHeight = 22.sp)
                is QuickCreateSeed.Image -> NoteThumbnail(seed.image, Modifier.size(96.dp))
            }
            error?.let { Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
        }
    },
    confirmButton = { Button(onClick = create, colors = ButtonDefaults.buttonColors(containerColor = Brand)) { Text("创建笔记", fontSize = 13.sp) } },
    dismissButton = { TextButton(onClick = dismiss) { Text("取消", color = InkMuted, fontSize = 13.sp) } },
)

private val DISPLAY = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss").withZone(ZoneId.systemDefault())
private val CHINESE_WEEKDAYS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

private fun recentQuickCreateCandidates(context: Context): List<QuickCreateCandidate> =
    listOfNotNull(recentClipboardCandidate(context)) + recentPhotoCandidates(context)

private fun recentClipboardCandidate(context: Context): QuickCreateCandidate? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    val clip = clipboard.primaryClip ?: return null
    val description = clipboard.primaryClipDescription ?: return null
    if (!description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) return null
    return clip.getItemAt(0)?.coerceToText(context)?.toString()?.let { textQuickCreateCandidate(it, System.currentTimeMillis()) }
}

private fun recentPhotoCandidates(context: Context): List<QuickCreateCandidate> {
    val permission = photoReadPermission()
    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) return emptyList()
    val cutoff = System.currentTimeMillis() / 1000 - 30
    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
    val candidates = mutableListOf<QuickCreateCandidate>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Images.Media.DATE_ADDED} >= ?",
        arrayOf(cutoff.toString()),
        "${MediaStore.Images.Media.DATE_ADDED} DESC",
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0).toString())
            candidates += imageQuickCreateCandidate(uri.toString(), cursor.getLong(1) * 1_000)
        }
    }
    return candidates
}

private fun latestRecentPhoto(context: Context): Uri? =
    recentPhotoCandidates(context)
        .maxByOrNull { it.timestampMillis }
        ?.let { (it.seed as QuickCreateSeed.Image).image.url }
        ?.let(Uri::parse)

private fun photoReadPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

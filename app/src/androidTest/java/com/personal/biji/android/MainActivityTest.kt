package com.personal.biji.android

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import com.personal.biji.android.data.local.BijiDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun launchActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("biji.db")
        context.getSharedPreferences("biji_storage", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("biji_theme_preference", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("biji_font_preference", Context.MODE_PRIVATE).edit().clear().commit()
        context
            .getSharedPreferences("biji_quick_create", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putBoolean("asked_for_photo_permission", true)
            .commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun mainNavigationShowsFourTabs() {
        compose.onAllNodesWithText("最近")[0].assertIsDisplayed()
        compose.onNodeWithText("书籍").assertIsDisplayed()
        compose.onNodeWithText("搜索").assertIsDisplayed()
        compose.onNodeWithText("我的").assertIsDisplayed()
    }

    @Test
    fun myScreenShowsStatsPanel() {
        compose.onNodeWithText("我的").performClick()
        compose.onNodeWithText("吾记统计").assertIsDisplayed()
        compose.onNodeWithText("存储模式").assertIsDisplayed()
    }

    @Test
    fun myScreenShowsAppearanceOptionsAndCanSwitchTheme() {
        compose.onNodeWithText("我的").performClick()

        compose.onNodeWithText("外观").assertIsDisplayed()
        compose.onNodeWithText("跟随系统").assertIsDisplayed()
        compose.onNodeWithText("深色").performClick()
        compose.onNodeWithText("浅色").performClick()

        compose.onNodeWithText("阅读字体").assertIsDisplayed()
    }

    @Test
    fun editorShowsImageActionsInFormatToolbar() {
        compose.onNodeWithContentDescription("新建笔记").performClick()

        compose.onNodeWithText("新建笔记").assertIsDisplayed()
        compose.onNodeWithContentDescription("拍照插入").assertIsDisplayed()
        compose.onNodeWithContentDescription("从相册插入").assertIsDisplayed()
        compose.onNodeWithContentDescription("扫描摘抄").assertIsDisplayed()
        compose.onNodeWithContentDescription("加粗").assertIsDisplayed()
        compose.onNodeWithContentDescription("斜体").assertIsDisplayed()
        compose.onNodeWithContentDescription("下划线").assertIsDisplayed()
        compose.onNodeWithContentDescription("标题").assertIsDisplayed()
    }

    @Test
    fun systemBackFromBookDetailReturnsToBooksScreen() {
        compose.onNodeWithText("书籍").performClick()
        compose.onNodeWithContentDescription("添加书籍").performClick()
        compose.onNodeWithText("书名").performTextInput("返回测试书")
        compose.onNodeWithText("保存").performClick()
        compose.onNodeWithText("《返回测试书》").performClick()

        pressBack()

        compose.onNodeWithText("我的书架").assertIsDisplayed()
        compose.onNodeWithText("《返回测试书》").assertIsDisplayed()
    }

    @Test
    fun deletingBookRequiresConfirmation() {
        compose.onNodeWithText("书籍").performClick()
        compose.onNodeWithContentDescription("添加书籍").performClick()
        compose.onNodeWithText("书名").performTextInput("删除确认测试书")
        compose.onNodeWithText("保存").performClick()
        compose.onNodeWithText("《删除确认测试书》").performClick()
        compose.onNodeWithContentDescription("删除书籍").performClick()

        compose.onNodeWithText("删除这本书？").assertIsDisplayed()
        compose.onNodeWithText("取消").performClick()
        compose.onNodeWithText("《删除确认测试书》").assertIsDisplayed()
    }

    @Test
    fun backFromNoteOpenedInsideBookDetailReturnsToBookDetail() {
        createBook("书内返回测试书")
        compose.onNodeWithText("《书内返回测试书》").performClick()
        compose.onNodeWithContentDescription("新建笔记").performClick()
        onView(withHint("写下你的读书笔记...")).perform(replaceText("书内返回测试笔记"))
        compose.onNodeWithText("保存").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("书内返回测试笔记", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("书内返回测试笔记").performClick()

        pressBack()

        compose.onNodeWithText("书中笔记").assertIsDisplayed()
        compose.onNodeWithText("书内返回测试笔记").assertIsDisplayed()
    }

    @Test
    fun booksListKeepsScrollPositionAfterOpeningDetailAndReturning() {
        repeat(24) { index -> createBook("滚动测试书${index.toString().padStart(2, '0')}") }
        compose.onNodeWithTag("books_list").performScrollToNode(hasText("《滚动测试书20》"))
        compose.onNodeWithText("《滚动测试书20》").assertIsDisplayed()
        compose.onNodeWithText("《滚动测试书20》").performClick()

        pressBack()

        compose.onNodeWithText("《滚动测试书20》").assertIsDisplayed()
        compose.onAllNodesWithText("《滚动测试书00》").assertCountEquals(0)
    }

    @Test
    fun editorShowsBuiltInTagsFirst() {
        compose.onNodeWithContentDescription("新建笔记").performClick()

        compose.onAllNodesWithText("原文摘抄")[0].assertIsDisplayed()
        compose.onAllNodesWithText("我的想法")[0].assertIsDisplayed()
    }

    @Test
    fun builtInTagsCannotBeDeletedFromTagManagement() {
        compose.onNodeWithText("我的").performClick()
        compose.onNodeWithText("标签管理").performClick()

        runBlocking {
            val dao = BijiDatabase.create(ApplicationProvider.getApplicationContext()).libraryDao()
            compose.waitUntil(timeoutMillis = 5_000) {
                runBlocking { dao.tags("Local").any { it.json.contains("原文摘抄") } }
            }
        }
        compose.onNodeWithTag("tags_list").performScrollToNode(hasText("原文摘抄", substring = true))
        compose.onAllNodesWithText("原文摘抄", substring = true)[0].assertIsDisplayed()
        compose.onNodeWithTag("tags_list").performScrollToNode(hasText("系统标签"))
        compose.onAllNodesWithText("系统标签")[0].assertIsDisplayed()
    }

    @Test
    fun bookDetailFiltersByBuiltInTagsAndExportsCurrentBook() {
        createBook("聚合测试书")
        compose.onNodeWithText("《聚合测试书》").performClick()
        compose.onNodeWithContentDescription("新建笔记").performClick()
        compose.onAllNodesWithText("原文摘抄")[0].performClick()
        onView(withHint("写下你的读书笔记...")).perform(replaceText("聚合测试摘抄"))
        compose.onNodeWithText("保存").performClick()

        compose.onNodeWithText("原文摘抄 1").performClick()
        compose.onNodeWithText("聚合测试摘抄").assertIsDisplayed()
        compose.onNodeWithContentDescription("导出 Markdown").performClick()
        compose.onNodeWithText("Markdown 导出").assertIsDisplayed()
        compose.onNodeWithText("# 《聚合测试书》", substring = true).assertIsDisplayed()
    }

    private fun createBook(title: String) {
        compose.onAllNodesWithText("书籍")[0].performClick()
        compose.onNodeWithContentDescription("添加书籍").performClick()
        compose.onNodeWithText("书名").performTextInput(title)
        compose.onNodeWithText("保存").performClick()
    }

}

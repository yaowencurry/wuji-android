package com.personal.biji.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.StorageMode
import com.personal.biji.android.ui.SharedPreferencesRecentBookStore
import com.personal.biji.android.ui.resolveDefaultBookId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecentBookStoreTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("biji_recent_books", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("biji_recent_books", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun storesRecentBooksSeparatelyForEachStorageMode() {
        val store = SharedPreferencesRecentBookStore(context)

        store.set(StorageMode.Local, "local-book")
        store.set(StorageMode.Remote, "remote-book")

        assertEquals("local-book", store.get(StorageMode.Local))
        assertEquals("remote-book", store.get(StorageMode.Remote))
    }

    @Test
    fun defaultBookFallsBackWhenRecentBookNoLongerExists() {
        val books = listOf(Book(id = "first", title = "第一本"), Book(id = "second", title = "第二本"))

        assertEquals("second", resolveDefaultBookId(books, "second"))
        assertEquals("first", resolveDefaultBookId(books, "deleted"))
    }
}

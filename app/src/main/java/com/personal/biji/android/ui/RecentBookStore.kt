package com.personal.biji.android.ui

import android.content.Context
import com.personal.biji.android.domain.Book
import com.personal.biji.android.domain.StorageMode

interface RecentBookStore {
    fun get(mode: StorageMode): String?
    fun set(mode: StorageMode, bookId: String)
}

class SharedPreferencesRecentBookStore(context: Context) : RecentBookStore {
    private val preferences = context.getSharedPreferences("biji_recent_books", Context.MODE_PRIVATE)

    override fun get(mode: StorageMode): String? =
        preferences.getString(mode.name, null)

    override fun set(mode: StorageMode, bookId: String) {
        preferences.edit().putString(mode.name, bookId).apply()
    }
}

fun resolveDefaultBookId(books: List<Book>, recentBookId: String?): String? =
    books.firstOrNull { it.id == recentBookId }?.id ?: books.firstOrNull()?.id

package com.personal.biji.android.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "books", primaryKeys = ["id"])
data class BookEntity(val id: String, val storageMode: String = "Local", val json: String, val deletedAt: String? = null)

@Entity(tableName = "notes", primaryKeys = ["id"])
data class NoteEntity(val id: String, val storageMode: String = "Local", val bookId: String, val json: String, val deletedAt: String? = null)

@Entity(tableName = "tags", primaryKeys = ["id"])
data class TagEntity(val id: String, val storageMode: String = "Local", val json: String, val deletedAt: String? = null)

@Entity(tableName = "note_tags", primaryKeys = ["noteId", "tagId"])
data class NoteTagEntity(val noteId: String, val tagId: String, val storageMode: String = "Local", val deletedAt: String? = null)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM books WHERE storageMode = :storageMode")
    fun observeBooks(storageMode: String): Flow<List<BookEntity>>
    @Query("SELECT * FROM notes WHERE storageMode = :storageMode")
    fun observeNotes(storageMode: String): Flow<List<NoteEntity>>
    @Query("SELECT * FROM tags WHERE storageMode = :storageMode")
    fun observeTags(storageMode: String): Flow<List<TagEntity>>
    @Query("SELECT * FROM note_tags WHERE storageMode = :storageMode")
    fun observeNoteTags(storageMode: String): Flow<List<NoteTagEntity>>

    @Query("SELECT * FROM books WHERE storageMode = :storageMode")
    suspend fun books(storageMode: String): List<BookEntity>
    @Query("SELECT * FROM notes WHERE storageMode = :storageMode")
    suspend fun notes(storageMode: String): List<NoteEntity>
    @Query("SELECT * FROM tags WHERE storageMode = :storageMode")
    suspend fun tags(storageMode: String): List<TagEntity>
    @Query("SELECT * FROM note_tags WHERE storageMode = :storageMode")
    suspend fun noteTags(storageMode: String): List<NoteTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(values: List<BookEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotes(values: List<NoteEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(values: List<TagEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNoteTags(values: List<NoteTagEntity>)

    @Query("DELETE FROM books WHERE storageMode = :storageMode")
    suspend fun clearBooks(storageMode: String)
    @Query("DELETE FROM notes WHERE storageMode = :storageMode")
    suspend fun clearNotes(storageMode: String)
    @Query("DELETE FROM tags WHERE storageMode = :storageMode")
    suspend fun clearTags(storageMode: String)
    @Query("DELETE FROM note_tags WHERE storageMode = :storageMode")
    suspend fun clearNoteTags(storageMode: String)
}

@Database(
    entities = [BookEntity::class, NoteEntity::class, TagEntity::class, NoteTagEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class BijiDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN storageMode TEXT NOT NULL DEFAULT 'Local'")
                db.execSQL("ALTER TABLE notes ADD COLUMN storageMode TEXT NOT NULL DEFAULT 'Local'")
                db.execSQL("ALTER TABLE tags ADD COLUMN storageMode TEXT NOT NULL DEFAULT 'Local'")
                db.execSQL("ALTER TABLE note_tags ADD COLUMN storageMode TEXT NOT NULL DEFAULT 'Local'")
            }
        }

        fun create(context: Context): BijiDatabase =
            Room.databaseBuilder(context, BijiDatabase::class.java, "biji.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

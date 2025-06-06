package com.example.mineprompt.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mineprompt.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_PROMPTS = "prompts"
        private const val TABLE_PROMPT_CATEGORIES = "prompt_categories"
        private const val TABLE_USER_LIKES = "user_likes"
        private const val TABLE_USER_FAVORITE_CATEGORIES = "user_favorite_categories"
        private const val TABLE_SEARCH_HISTORY = "search_history"

        // 공통 컬럼
        private const val COLUMN_ID = "id"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"

        // Users 테이블 컬럼
        private const val COLUMN_USER_NICKNAME = "nickname"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"

        // Categories 테이블 컬럼
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_DESCRIPTION = "description"
        private const val COLUMN_IS_ACTIVE = "is_active"

        // Prompts 테이블 컬럼
        private const val COLUMN_PROMPT_TITLE = "title"
        private const val COLUMN_PROMPT_CONTENT = "content"
        private const val COLUMN_PROMPT_PURPOSE = "purpose"
        private const val COLUMN_PROMPT_KEYWORDS = "keywords"
        private const val COLUMN_PROMPT_LENGTH = "length"
        private const val COLUMN_PROMPT_STYLE = "style"
        private const val COLUMN_PROMPT_LANGUAGE = "language"
        private const val COLUMN_PROMPT_CREATOR_ID = "creator_id"
        private const val COLUMN_PROMPT_LIKE_COUNT = "like_count"
        private const val COLUMN_PROMPT_VIEW_COUNT = "view_count"

        // 관계 테이블 컬럼
        private const val COLUMN_PROMPT_ID = "prompt_id"
        private const val COLUMN_CATEGORY_ID = "category_id"
        private const val COLUMN_USER_ID = "user_id"

        // Search History 컬럼
        private const val COLUMN_SEARCH_KEYWORD = "keyword"
        private const val COLUMN_SEARCHED_AT = "searched_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NICKNAME TEXT NOT NULL UNIQUE,
                $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL UNIQUE,
                $COLUMN_CATEGORY_DESCRIPTION TEXT,
                $COLUMN_IS_ACTIVE INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()

        val createPromptsTable = """
            CREATE TABLE $TABLE_PROMPTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROMPT_TITLE TEXT NOT NULL,
                $COLUMN_PROMPT_CONTENT TEXT NOT NULL,
                $COLUMN_PROMPT_PURPOSE TEXT,
                $COLUMN_PROMPT_KEYWORDS TEXT,
                $COLUMN_PROMPT_LENGTH TEXT NOT NULL DEFAULT 'MEDIUM',
                $COLUMN_PROMPT_STYLE TEXT,
                $COLUMN_PROMPT_LANGUAGE TEXT NOT NULL DEFAULT '한국어',
                $COLUMN_PROMPT_CREATOR_ID INTEGER NOT NULL,
                $COLUMN_PROMPT_LIKE_COUNT INTEGER NOT NULL DEFAULT 0,
                $COLUMN_PROMPT_VIEW_COUNT INTEGER NOT NULL DEFAULT 0,
                $COLUMN_IS_ACTIVE INTEGER NOT NULL DEFAULT 1,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_PROMPT_CREATOR_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        val createPromptCategoriesTable = """
            CREATE TABLE $TABLE_PROMPT_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROMPT_ID INTEGER NOT NULL,
                $COLUMN_CATEGORY_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_PROMPT_ID) REFERENCES $TABLE_PROMPTS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COLUMN_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_ID) ON DELETE CASCADE,
                UNIQUE($COLUMN_PROMPT_ID, $COLUMN_CATEGORY_ID)
            )
        """.trimIndent()

        val createUserLikesTable = """
            CREATE TABLE $TABLE_USER_LIKES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_PROMPT_ID INTEGER NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COLUMN_PROMPT_ID) REFERENCES $TABLE_PROMPTS($COLUMN_ID) ON DELETE CASCADE,
                UNIQUE($COLUMN_USER_ID, $COLUMN_PROMPT_ID)
            )
        """.trimIndent()

        val createUserFavoriteCategoriesTable = """
            CREATE TABLE $TABLE_USER_FAVORITE_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_CATEGORY_ID INTEGER NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COLUMN_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_ID) ON DELETE CASCADE,
                UNIQUE($COLUMN_USER_ID, $COLUMN_CATEGORY_ID)
            )
        """.trimIndent()

        val createSearchHistoryTable = """
            CREATE TABLE $TABLE_SEARCH_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_SEARCH_KEYWORD TEXT NOT NULL,
                $COLUMN_SEARCHED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createPromptsTable)
        db.execSQL(createPromptCategoriesTable)
        db.execSQL(createUserLikesTable)
        db.execSQL(createUserFavoriteCategoriesTable)
        db.execSQL(createSearchHistoryTable)

        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SEARCH_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_FAVORITE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_LIKES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROMPT_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROMPTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        CategoryType.values().forEach { categoryType ->
            val values = ContentValues().apply {
                put(COLUMN_ID, categoryType.id)
                put(COLUMN_CATEGORY_NAME, categoryType.displayName)
                put(COLUMN_CATEGORY_DESCRIPTION, categoryType.code)
                put(COLUMN_IS_ACTIVE, 1)
            }
            db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_prompts_creator ON $TABLE_PROMPTS($COLUMN_PROMPT_CREATOR_ID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_prompts_created_at ON $TABLE_PROMPTS($COLUMN_CREATED_AT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_prompts_like_count ON $TABLE_PROMPTS($COLUMN_PROMPT_LIKE_COUNT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_likes_user ON $TABLE_USER_LIKES($COLUMN_USER_ID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_search_history_user ON $TABLE_SEARCH_HISTORY($COLUMN_USER_ID)")
    }
}
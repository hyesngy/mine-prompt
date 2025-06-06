package com.example.mineprompt.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

object  DefaultUserSeeder {

    fun createDefaultUsers(db: SQLiteDatabase) {
        val defaultUsers = listOf(
            mapOf(
                "id" to 1,
                "nickname" to "마인프롬프트",
                "email" to "admin@mineprompt.com",
                "password" to "admin123",
                "created_at" to System.currentTimeMillis(),
                "updated_at" to System.currentTimeMillis()
            ),
            mapOf(
                "id" to 2,
                "nickname" to "나그네",
                "email" to "writer@mineprompt.com",
                "password" to "writer123",
                "created_at" to System.currentTimeMillis(),
                "updated_at" to System.currentTimeMillis()
            ),
            mapOf(
                "id" to 3,
                "nickname" to "아무개",
                "email" to "writer2@mineprompt.com",
                "password" to "writer123",
                "created_at" to System.currentTimeMillis(),
                "updated_at" to System.currentTimeMillis()
            )
        )

        defaultUsers.forEach { user ->
            val values = ContentValues().apply {
                user.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Long -> put(key, value)
                        is Int -> put(key, value.toLong())
                    }
                }
            }
            db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }
}
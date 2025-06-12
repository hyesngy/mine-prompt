// app/src/main/java/com/example/mineprompt/data/DataInitializer.kt

package com.example.mineprompt.data

import android.content.Context
import android.util.Log

class DataInitializer(private val context: Context) {

    companion object {
        private const val TAG = "DataInitializer"
        private const val PREFS_NAME = "mineprompt_prefs"
        private const val KEY_DATA_INITIALIZED = "data_initialized"
        private const val KEY_JSON_DATA_VERSION = "json_data_version"
        private const val CURRENT_JSON_VERSION = 1 // JSON 데이터 버전 관리
    }

    private val databaseHelper = DatabaseHelper(context)
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun initializeAppData() {
        Log.d(TAG, "앱 데이터 초기화 시작")

        if (!isDataInitialized() || needsJsonDataUpdate()) {
            Log.d(TAG, "기본 데이터 설정 중...")
            setupAllData()
            markDataAsInitialized()
            updateJsonDataVersion()
            Log.d(TAG, "기본 데이터 설정 완료")
        } else {
            Log.d(TAG, "이미 초기화된 데이터가 존재합니다.")
        }

        // 데이터 통계 로그
        logDataStatistics()
        Log.d(TAG, "앱 데이터 초기화 완료")
    }

    private fun setupAllData() {
        try {
            val db = databaseHelper.writableDatabase
            DefaultUserSeeder.createDefaultUsers(db)
            JsonDataLoader.loadPromptsFromJson(context, db)
            PromptCategoryMapper.setupPromptCategoryMappings(db)

        } catch (e: Exception) {
            Log.e(TAG, "데이터 설정 중 오류 발생", e)
        }
    }

    private fun isDataInitialized(): Boolean {
        return sharedPrefs.getBoolean(KEY_DATA_INITIALIZED, false)
    }

    private fun needsJsonDataUpdate(): Boolean {
        val savedVersion = sharedPrefs.getInt(KEY_JSON_DATA_VERSION, 0)
        return savedVersion < CURRENT_JSON_VERSION
    }

    private fun markDataAsInitialized() {
        sharedPrefs.edit().putBoolean(KEY_DATA_INITIALIZED, true).apply()
    }

    private fun updateJsonDataVersion() {
        sharedPrefs.edit().putInt(KEY_JSON_DATA_VERSION, CURRENT_JSON_VERSION).apply()
    }

    private fun logDataStatistics() {
        try {
            val db = databaseHelper.readableDatabase

            // 전체 프롬프트 수
            val totalPrompts = db.rawQuery("SELECT COUNT(*) FROM prompts WHERE is_active = 1", null)
            totalPrompts.moveToFirst()
            val promptCount = totalPrompts.getInt(0)
            totalPrompts.close()

        } catch (e: Exception) {
            Log.e(TAG, "데이터 통계 조회 실패", e)
        }
    }

    fun forceUpdateJsonData() {
        Log.d(TAG, "JSON 데이터 강제 업데이트 시작")

        try {
            val db = databaseHelper.writableDatabase

            // 기존 프롬프트 데이터 삭제
            db.delete("prompts", null, null)

            // JSON 데이터 다시 로드
            JsonDataLoader.loadPromptsFromJson(context, db)
            updateJsonDataVersion()

            Log.d(TAG, "JSON 데이터 강제 업데이트 완료")

        } catch (e: Exception) {
            Log.e(TAG, "JSON 데이터 강제 업데이트 실패", e)
        }
    }
}
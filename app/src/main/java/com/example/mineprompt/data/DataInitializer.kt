package com.example.mineprompt.data

import android.content.Context
import android.util.Log

class DataInitializer(private val context: Context) {

    companion object {
        private const val TAG = "DataInitializer"
        private const val PREFS_NAME = "mineprompt_prefs"
        private const val KEY_DATA_INITIALIZED = "data_initialized"
    }

    private val databaseHelper = DatabaseHelper(context)
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun initializeAppData() {
        Log.d(TAG, "앱 데이터 초기화 시작")

        if (!isDataInitialized()) {
            Log.d(TAG, "기본 데이터 설정 중...")
            setupAllData()
            markDataAsInitialized()
            Log.d(TAG, "기본 데이터 설정 완료")
        } else {
            Log.d(TAG, "이미 초기화된 데이터가 존재합니다.")
        }

        Log.d(TAG, "앱 데이터 초기화 완료")
    }

    private fun setupAllData() {
        try {
            val db = databaseHelper.writableDatabase

            DefaultUserSeeder.createDefaultUsers(db)
            SamplePromptSeeder.loadSamplePrompts(db)
            PromptCategoryMapper.setupPromptCategoryMappings(db)

        } catch (e: Exception) {
            Log.e(TAG, "데이터 설정 중 오류 발생", e)
        }
    }

    private fun isDataInitialized(): Boolean {
        return sharedPrefs.getBoolean(KEY_DATA_INITIALIZED, false)
    }

    private fun markDataAsInitialized() {
        sharedPrefs.edit().putBoolean(KEY_DATA_INITIALIZED, true).apply()
    }

}
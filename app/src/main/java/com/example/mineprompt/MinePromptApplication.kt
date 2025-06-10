package com.example.mineprompt

import android.app.Application
import com.example.mineprompt.data.DataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MinePromptApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDatabase()
    }

    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataInitializer = DataInitializer(this@MinePromptApplication)
                dataInitializer.initializeAppData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
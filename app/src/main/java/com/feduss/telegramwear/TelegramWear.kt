package com.feduss.telegramwear

import android.app.Application
import com.feduss.telegramwear.repos.ClientRepository
import java.io.File

class TelegramWear: Application() {

    override fun onCreate() {
        super.onCreate()
        this.setupClient()
    }

    private fun setupClient() {
        val appDir = getExternalFilesDir(null).toString()
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists())
            dir.mkdirs()
        ClientRepository.setupHandler(appDir)
    }
}
package com.feduss.telegramwear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.feduss.telegramwear.repos.ClientRepository
import org.drinkless.td.libcore.telegram.Client
import java.io.File


internal class LoginFlowActivityViewModel(application: Application) :
    AndroidViewModel(application) {

    init {
        val appDir =  application.getExternalFilesDir(null).toString()
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists())
            dir.mkdirs()
        ClientRepository.setupHandler(appDir)
    }
}
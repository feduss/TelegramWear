package com.feduss.telegramwear

import androidx.lifecycle.ViewModel
import com.feduss.telegramwear.repos.ClientRepository
import java.io.File


internal class LoginFlowActivityViewModel : ViewModel() {
    lateinit var clientRepository: ClientRepository

    fun setClientRepository(appDir: String) {
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        this.clientRepository = ClientRepository
        this.clientRepository.setupHandler(appDir)
    }
}
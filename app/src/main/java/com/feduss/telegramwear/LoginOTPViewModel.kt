package com.feduss.telegramwear

import androidx.lifecycle.ViewModel
import java.io.File

class LoginOTPViewModel : ViewModel() {
    private lateinit var clientRepository: ClientRepository

    fun setClientRepository(appDir: String, phoneNumber: String) {
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        this.clientRepository = ClientRepository(appDir)
        this.clientRepository.setClient(phoneNumber)
    }
}
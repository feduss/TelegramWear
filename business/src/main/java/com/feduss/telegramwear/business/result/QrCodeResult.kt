package com.feduss.telegramwear.business.result

sealed class QrCodeResult {
    data class ValidQrCode(var link: String): QrCodeResult()
    data object Error: QrCodeResult()
}

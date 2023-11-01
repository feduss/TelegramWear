package com.feduss.telegram.entity

sealed class QrCodeResult {
    data class ValidQrCode(var link: String): QrCodeResult()
    data object Error: QrCodeResult()
}

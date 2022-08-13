package com.feduss.telegramwear.login.authType

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.feduss.telegramwear.repos.ClientRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter


class LoginViewModel(application: Application) :
    AndroidViewModel(application) {
    var qrCode = MutableLiveData<Bitmap?>(null)
    val qrCodeSize = 100

    fun fetchQrCode() {
        ClientRepository.fetchQRCodeLink { link ->
            generateQrCodeFromLink(link)
        }
    }

    private fun generateQrCodeFromLink(link: String) {
        val writer = QRCodeWriter()
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = Integer.valueOf(0)
        val bitMatrix = writer.encode(link, BarcodeFormat.QR_CODE, 100, 100, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        this.qrCode.postValue(bitmap)
    }
}
package com.feduss.telegramwear.viewmodel.login.authtype.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.business.result.QrCodeResult
import com.feduss.telegramwear.business.ClientInteractor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginAuthQrCodeViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
) : ViewModel() {

    sealed class State {
        data object GoTo2FA: State()
    }

    val mainHandler = Handler(Looper.getMainLooper())

    private val qrCodeSize: Int = 150
    private val qrCodeExpirationInSec = 5L

    private val _state = MutableStateFlow<State?>(null)
    var state = _state.asStateFlow()

    private val _qrCode = MutableStateFlow<Bitmap?>(null)
    var qrCode = _qrCode.asStateFlow()

    init {

        repeatingFun(
            func = {
                _qrCode.value = null
                viewModelScope.launch {
                    fetchQrCode()
                }
            },
            seconds = qrCodeExpirationInSec
        )

        viewModelScope.launch {
            check2FA()
        }
    }

    private fun repeatingFun(func: (() -> Unit), seconds: Long) {
        mainHandler.post(object : Runnable {
            override fun run() {
                func()
                mainHandler.postDelayed(this, seconds * 1000)
            }
        })
    }

    private suspend fun fetchQrCode() {
        clientInteractor.retrieveQrCode().collectLatest() { qrCodeResult ->
            if (qrCodeResult is QrCodeResult.ValidQrCode) {
                generateQrCodeFromLink(qrCodeResult.link)
            } else {
                _qrCode.value = null
            }
        }
    }

    private fun generateQrCodeFromLink(link: String) {
        val writer = QRCodeWriter()
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = Integer.valueOf(0)
        val bitMatrix = writer.encode(link, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        _qrCode.value = bitmap
    }

    private suspend fun check2FA() {
        clientInteractor.getStatus().asFlow().collect{ authStatus ->
            when(authStatus) {
                AuthStatus.Waiting2FA -> _state.value = State.GoTo2FA
                else -> {}
            }
        }
    }
}
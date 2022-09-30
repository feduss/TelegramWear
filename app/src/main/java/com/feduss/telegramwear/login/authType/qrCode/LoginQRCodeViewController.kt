package com.feduss.telegramwear.login.authType.qrCode

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.feduss.telegramwear.R
import com.google.android.material.button.MaterialButton

class LoginQRCodeViewController : Fragment() {

    lateinit var qrCodeContainer: FrameLayout
    lateinit var qrCodeImage: ImageView
    lateinit var loadingBar: ProgressBar

    private val viewModel: LoginQRCodeViewModel by viewModels()

    companion object {
        fun newInstance() = LoginQRCodeViewController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_qrcode_view_controller, container, false)

        this.qrCodeContainer = view.findViewById(R.id.qrcodeContainer)
        this.qrCodeImage = view.findViewById(R.id.qrCodeImage)
        this.loadingBar = view.findViewById(R.id.qrCodeLoadingBar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchQrCode()

        viewModel.qrCode.observe(viewLifecycleOwner) { qrCodeBitmap ->
            if (qrCodeBitmap != null) {
                this.qrCodeImage.setImageBitmap(qrCodeBitmap)
                this.loadingBar.isVisible = false
                this.qrCodeContainer.setBackgroundColor(Color.WHITE)
                this.qrCodeImage.isVisible = true
            }

        }
    }

}
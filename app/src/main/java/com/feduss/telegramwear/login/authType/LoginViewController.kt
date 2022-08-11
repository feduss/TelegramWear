package com.feduss.telegramwear.login.authType

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.feduss.telegramwear.R
import com.google.android.material.button.MaterialButton

class LoginViewController : Fragment() {

    lateinit var qrCodeContainer: FrameLayout
    lateinit var qrCodeImage: ImageView
    lateinit var loadingBar: ProgressBar
    lateinit var numberButton: MaterialButton

    companion object {
        fun newInstance() = LoginViewController()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_page_view_controller, container, false)

        this.qrCodeContainer = view.findViewById(R.id.qrcodeContainer)
        this.qrCodeImage = view.findViewById(R.id.qrCodeImage)
        this.loadingBar = view.findViewById(R.id.qrCodeLoadingBar)
        this.numberButton = view.findViewById(R.id.numberButton)

        this.numberButton.setOnClickListener {
            findNavController().navigate(R.id.goToPhoneNumberPage)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
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
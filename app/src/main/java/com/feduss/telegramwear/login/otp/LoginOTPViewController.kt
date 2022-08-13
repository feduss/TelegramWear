package com.feduss.telegramwear.login.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.feduss.telegramwear.R

class LoginOTPViewController : Fragment() {

    private val args: LoginOTPViewControllerArgs by navArgs()
    private val viewModel: LoginOTPViewModel by viewModels()

    companion object {
        fun newInstance() = LoginOTPViewController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_otp_view_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val phoneNumber = args.phoneNumber
        //TODO: add send otp
        //viewModel.setClientRepository(appDir, phoneNumber)
    }

}
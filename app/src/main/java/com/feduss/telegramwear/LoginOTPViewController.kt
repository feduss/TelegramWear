package com.feduss.telegramwear

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

class LoginOTPViewController : Fragment() {

    val args: LoginOTPViewControllerArgs by navArgs()

    companion object {
        fun newInstance() = LoginOTPViewController()
    }

    private lateinit var viewModel: LoginOTPViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_otp_view_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginOTPViewModel::class.java)
        val appDir = requireActivity().getExternalFilesDir(null).toString()
        val phoneNumber = args.phoneNumber
        viewModel.setClientRepository(appDir, phoneNumber)
    }

}
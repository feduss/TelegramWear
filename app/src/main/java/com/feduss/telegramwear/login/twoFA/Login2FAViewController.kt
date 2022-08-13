package com.feduss.telegramwear.login.twoFA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.feduss.telegramwear.R

class Login2FAViewController : Fragment() {

    private val viewModel: Login2FAViewModel by viewModels()

    companion object {
        fun newInstance() = Login2FAViewController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_2fa_view_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
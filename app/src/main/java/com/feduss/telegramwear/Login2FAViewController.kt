package com.feduss.telegramwear

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Login2FAViewController : Fragment() {

    companion object {
        fun newInstance() = Login2FAViewController()
    }

    private lateinit var viewModel: Login2FAViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_2fa_view_controller, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(Login2FAViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
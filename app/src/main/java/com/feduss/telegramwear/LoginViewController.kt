package com.feduss.telegramwear

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton

class LoginViewController : Fragment() {

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

        this.numberButton = view.findViewById(R.id.numberButton)

        this.numberButton.setOnClickListener {
            findNavController().navigate(R.id.goToPhoneNumberPage)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        //viewModel.fetchQrCode()
    }

}
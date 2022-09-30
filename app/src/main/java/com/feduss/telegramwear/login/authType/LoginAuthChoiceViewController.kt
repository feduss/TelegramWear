package com.feduss.telegramwear.login.authType

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.feduss.telegramwear.R
import com.google.android.material.button.MaterialButton

class LoginAuthChoiceViewController : Fragment() {

    private lateinit var authUrlButton: MaterialButton
    private lateinit var authQRCodeButton: MaterialButton
    private lateinit var authPhoneNumberButton: MaterialButton


    private val viewModel: LoginAuthChoiceViewModel by viewModels()

    companion object {
        fun newInstance() = LoginAuthChoiceViewController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_login_auth_choice_view_controller,
            container,
            false)

        this.authUrlButton = view.findViewById(R.id.authUrlButton)
        this.authQRCodeButton = view.findViewById(R.id.authQRCodeButton)
        this.authPhoneNumberButton = view.findViewById(R.id.authPhoneNumberButton)

        this.authUrlButton.setOnClickListener {
            findNavController().navigate(R.id.goToLoginUrlPage)
        }

        this.authQRCodeButton.setOnClickListener {
            findNavController().navigate(R.id.goToLoginQRCodePage)
        }

        this.authPhoneNumberButton.setOnClickListener {
            findNavController().navigate(R.id.goToLoginPhoneNumberPage)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
package com.feduss.telegramwear.login.authType

import androidx.lifecycle.ViewModelProvider
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
    private lateinit var authOtherButton: MaterialButton

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
        this.authOtherButton = view.findViewById(R.id.authOtherButton)

        this.authUrlButton.setOnClickListener {
            findNavController().navigate(R.id.goToLoginUrlPage)
        }

        this.authOtherButton.setOnClickListener {
            findNavController().navigate(R.id.goToLoginPage)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
package com.feduss.telegramwear.login.authType.phoneNumber

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.feduss.telegramwear.R
import com.google.android.material.textfield.TextInputEditText

class LoginPhoneNumberViewController : Fragment() {

    private lateinit var confirmButton: Button
    private lateinit var inputNumberTextField: TextInputEditText

    private val viewModel: LoginPhoneNumberViewModel by viewModels()

    companion object {
        fun newInstance() = LoginPhoneNumberViewController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_number_view_controller, container, false)

        this.confirmButton = view.findViewById(R.id.confirmButton)
        this.confirmButton.isEnabled = false
        this.confirmButton.setOnClickListener {
            val action = LoginPhoneNumberViewControllerDirections.goToOTPPage(this.viewModel.phoneNumber)
            findNavController().navigate(action)
        }

        this.inputNumberTextField = view.findViewById(R.id.loginNumberValue)
        this.inputNumberTextField.doAfterTextChanged {
            this.viewModel.userHasUpdatedPhoneNumber(inputNumberTextField.text.toString())
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.isConfirmButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            this.confirmButton.isEnabled = isEnabled
        }
    }

}
package com.feduss.telegramwear.login.authType.phoneNumber

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.feduss.telegramwear.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText

class LoginPhoneNumberViewController : Fragment() {

    private lateinit var inputNumberTextField: TextInputEditText
    private lateinit var errorLabel: TextView
    private lateinit var confirmButton: Button
    private lateinit var progressIndicator: CircularProgressIndicator

    private val viewModel: LoginPhoneNumberViewModel by viewModels()

    companion object {
        fun newInstance() = LoginPhoneNumberViewController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_phone_number_view_controller, container, false)

        this.inputNumberTextField = view.findViewById(R.id.loginNumberValue)
        this.errorLabel = view.findViewById(R.id.errorLabel)
        this.confirmButton = view.findViewById(R.id.confirmButton)
        this.progressIndicator = view.findViewById(R.id.progressIndicator)

        this.inputNumberTextField.doAfterTextChanged {
            this.viewModel.userHasUpdatedPhoneNumber(inputNumberTextField.text.toString())
        }

        this.errorLabel.visibility = View.GONE

        this.confirmButton.isEnabled = false
        this.confirmButton.setOnClickListener {
            requireActivity().runOnUiThread {
                this.errorLabel.visibility = View.GONE
                this.confirmButton.visibility = View.GONE
                this.progressIndicator.visibility = View.VISIBLE
            }
            viewModel.sendOTP()

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.isConfirmButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            this.confirmButton.isEnabled = isEnabled
        }

        this.viewModel.isOTPSent.observe(viewLifecycleOwner) { isOtpSent ->
            isOtpSent?.let { isOtpSent ->
                requireActivity().runOnUiThread {
                    this.confirmButton.visibility = View.VISIBLE
                    this.progressIndicator.visibility = View.GONE
                }
                if (isOtpSent) {
                    findNavController().navigate(R.id.goToOTPPage)
                } else {
                    requireActivity().runOnUiThread {
                        this.errorLabel.text = "Si è verificato un problema durante l'invio del codice otp"
                        this.errorLabel.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
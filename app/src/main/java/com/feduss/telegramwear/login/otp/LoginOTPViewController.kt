package com.feduss.telegramwear.login.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.feduss.telegramwear.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText

class LoginOTPViewController : Fragment() {

    private val viewModel: LoginOTPViewModel by viewModels()

    private lateinit var inputOtpTextField: TextInputEditText
    private lateinit var errorLabel: TextView
    private lateinit var confirmButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator


    companion object {
        fun newInstance() = LoginOTPViewController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_otp_view_controller, container, false)

        this.inputOtpTextField = view.findViewById(R.id.otpValue)
        this.errorLabel = view.findViewById(R.id.errorLabel)
        this.confirmButton = view.findViewById(R.id.confirmButton)
        this.progressIndicator = view.findViewById(R.id.progressIndicator)
        this.confirmButton.isEnabled = false

        this.inputOtpTextField.doAfterTextChanged { text ->
            this.confirmButton.isEnabled = text.toString().isNotEmpty()
        }

        this.confirmButton.setOnClickListener {
            this.viewModel.userHasUpdatedOtp(this.inputOtpTextField.text.toString())
            this.confirmButton.visibility = View.GONE
            this.progressIndicator.visibility = View.VISIBLE
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.isOTPValid.observe(viewLifecycleOwner) { isOTPValid ->
            isOTPValid?.let {
                if (isOTPValid) {
                    requireActivity().runOnUiThread {
                        errorLabel.visibility = View.GONE
                    }
                    //TODO: to fix this check
                    if(this.viewModel.is2FAEnabled()) {
                        findNavController().navigate(R.id.goTo2FAPageFromOTP)
                    } else {
                        findNavController().navigate(R.id.goToChatListFromOTP)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        this.errorLabel.error = getString(R.string.login_otp_wrong_code)
                        this.errorLabel.visibility = View.VISIBLE
                        this.inputOtpTextField.text = null
                        this.confirmButton.isEnabled = false
                        this.confirmButton.visibility = View.VISIBLE
                        this.progressIndicator.visibility = View.GONE
                    }
                }
            }
        }
    }
}
package com.feduss.telegramwear.login.twoFA

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

class Login2FAViewController : Fragment() {

    private val viewModel: Login2FAViewModel by viewModels()

    private lateinit var inputPasswordTextField: TextInputEditText
    private lateinit var errorLabel: TextView
    private lateinit var confirmButton: Button
    private lateinit var progressIndicator: CircularProgressIndicator

    companion object {
        fun newInstance() = Login2FAViewController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_2fa_view_controller, container, false)

        this.inputPasswordTextField = view.findViewById(R.id.passwordValue)
        this.errorLabel = view.findViewById(R.id.errorLabel)
        this.confirmButton = view.findViewById(R.id.confirmButton)
        this.progressIndicator = view.findViewById(R.id.progressIndicator)

        this.inputPasswordTextField.doAfterTextChanged {
            this.viewModel.userHasUpdatedPassword(inputPasswordTextField.text.toString())
        }

        this.errorLabel.visibility = View.GONE

        this.confirmButton.isEnabled = false
        this.confirmButton.setOnClickListener {
            requireActivity().runOnUiThread {
                this.errorLabel.visibility = View.GONE
                this.confirmButton.visibility = View.GONE
                this.progressIndicator.visibility = View.VISIBLE
            }
            viewModel.checkPassword() { isSuccess ->
                requireActivity().runOnUiThread {
                    this.confirmButton.visibility = View.VISIBLE
                    this.progressIndicator.visibility = View.GONE
                }
                if (isSuccess) {
                    findNavController().navigate(R.id.goToOTPPage)
                } else {
                    requireActivity().runOnUiThread {
                        this.errorLabel.text = "Si è verificato un problema durante l'invio del codice otp"
                        this.errorLabel.visibility = View.VISIBLE
                    }
                }
            }

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
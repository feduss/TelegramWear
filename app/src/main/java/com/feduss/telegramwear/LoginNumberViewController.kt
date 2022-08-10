package com.feduss.telegramwear

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class LoginNumberViewController : Fragment() {

    private lateinit var confirmButton: Button
    private lateinit var inputNumberTextField: TextInputEditText

    companion object {
        fun newInstance() = LoginNumberViewController()
    }

    private lateinit var viewModel: LoginNumberViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_number_view_controller, container, false)

        this.confirmButton = view.findViewById(R.id.confirmButton)
        this.confirmButton.isEnabled = false
        this.confirmButton.setOnClickListener {
            val action = LoginNumberViewControllerDirections.goToOTPPage(this.viewModel.phoneNumber)
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

        this.viewModel = ViewModelProvider(this)[LoginNumberViewModel::class.java]
        this.viewModel.isConfirmButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            this.confirmButton.isEnabled = isEnabled
        }
    }

}
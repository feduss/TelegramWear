package com.feduss.telegramwear

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import java.io.File


class WelcomePageViewController : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome_page_view_controller, container, false)
        val startButton = view.findViewById<Button>(R.id.startButton)

        val dir = File(requireActivity().getExternalFilesDir(null), "TelegramWear/tdlib")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomePageViewController_to_loginPageViewController2)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {

        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            WelcomePageViewController().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
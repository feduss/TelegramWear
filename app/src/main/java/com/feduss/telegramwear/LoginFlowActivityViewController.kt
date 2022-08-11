package com.feduss.telegramwear

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.feduss.telegramwear.databinding.ActivityMainBinding

class LoginFlowActivityViewController : AppCompatActivity() {

    private lateinit var viewModel: LoginFlowActivityViewModel
    private lateinit var binding: ActivityMainBinding
    lateinit var navViewController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginFlowActivityViewModel::class.java]
        val appDir = getExternalFilesDir(null).toString()
        viewModel.setClientRepository(appDir)

    }
}
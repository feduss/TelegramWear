package com.feduss.telegramwear

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.feduss.telegramwear.databinding.ActivityMainBinding

class LoginFlowActivityViewController : AppCompatActivity() {

    private val viewModel: LoginFlowActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
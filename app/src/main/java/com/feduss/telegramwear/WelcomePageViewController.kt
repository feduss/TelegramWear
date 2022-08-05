package com.feduss.telegramwear

import android.app.Activity
import android.os.Bundle
import com.feduss.telegramwear.databinding.WelcomePageBinding
import com.google.android.material.button.MaterialButton

class WelcomePageViewController : Activity() {

    private lateinit var binding: WelcomePageBinding
    private lateinit var startButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.startButton = findViewById(R.id.startButton)

    }
}
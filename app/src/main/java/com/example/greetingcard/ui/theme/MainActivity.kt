package com.example.greetingcard.ui.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.greetingcard.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_first_layout)  // Use XML layout instead of Compose
    }
}

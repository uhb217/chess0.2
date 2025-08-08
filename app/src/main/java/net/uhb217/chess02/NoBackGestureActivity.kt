package net.uhb217.chess02

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

open class NoBackGestureActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this
            ,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {} // Do nothing to block back navigation
        })
    }
}
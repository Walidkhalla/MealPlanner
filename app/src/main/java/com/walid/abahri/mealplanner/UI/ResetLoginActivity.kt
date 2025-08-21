package com.walid.abahri.mealplanner.UI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.walid.abahri.mealplanner.R

/**
 * Simple activity to clear login state and help with testing
 */
class ResetLoginActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_login)
        
        findViewById<Button>(R.id.buttonClearLogin).setOnClickListener {
            // Clear login state
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply()
            
            Toast.makeText(this, "Login state cleared", Toast.LENGTH_LONG).show()
        }
        
        findViewById<Button>(R.id.buttonRestart).setOnClickListener {
            // Just finish this activity to return to previous screen
            finish()
        }
    }
}

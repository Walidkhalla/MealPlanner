package com.walid.abahri.mealplanner.util

import android.content.Context
import android.widget.Toast

/**
 * Utility functions for managing login state
 */
object LoginUtils {
    
    /**
     * Forces a logout by clearing the saved login state
     * @return true if logout was successful
     */
    fun forceLogout(context: Context): Boolean {
        return try {
            // Clear login state in SharedPreferences
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply()
            
            Toast.makeText(context, "Logout successful! Please restart the app.", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Error during logout: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}

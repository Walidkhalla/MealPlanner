package com.walid.abahri.mealplanner.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to manage user-related operations and information
 */
class UserManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null
        
        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                val instance = UserManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Get the current user ID, defaults to 1 if not found
     */
    fun getCurrentUserId(): Int {
        return prefs.getInt("current_user_id", 1)
    }
    
    /**
     * Save the current user ID in SharedPreferences
     */
    fun setCurrentUserId(userId: Int) {
        prefs.edit().putInt("current_user_id", userId).apply()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }
    
    /**
     * Set login status
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }
    
    /**
     * Store user info after login
     */
    fun storeUserInfo(userId: Int, username: String, fullName: String, email: String) {
        prefs.edit().apply {
            putInt("current_user_id", userId)
            putString("username", username)
            putString("userFullName", fullName)
            putString("userEmail", email)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }
    
    /**
     * Clear all user info on logout
     */
    fun clearUserInfo() {
        prefs.edit().apply {
            remove("current_user_id")
            remove("username")
            remove("userFullName")
            remove("userEmail")
            putBoolean("isLoggedIn", false)
            apply()
        }
    }
}

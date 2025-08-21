package com.walid.abahri.mealplanner.repository

import com.walid.abahri.mealplanner.DB.User
import com.walid.abahri.mealplanner.DB.UserDao

class UserRepository(private val userDao: UserDao) {
    /**
     * Insert or update a user in the database
     */
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    /**
     * Get a user by username
     */
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    /**
     * Validate login credentials
     */
    suspend fun validateLogin(username: String, password: String): Boolean {
        // Check if a user with given credentials exists
        val user = userDao.getUser(username, password)
        return user != null
    }
    
    /**
     * Get all users (for admin purposes)
     */
    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }
    
    /**
     * Update user profile information
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}

package com.walid.abahri.mealplanner.DB

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password_hash = :password")
    suspend fun getUser(username: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
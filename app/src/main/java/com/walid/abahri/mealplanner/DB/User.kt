package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    @ColumnInfo(name = "full_name")
    val fullName: String = "",

    @ColumnInfo(name = "daily_calorie_goal")
    val dailyCalorieGoal: Int? = 2000,

    @ColumnInfo(name = "dietary_preferences")
    val dietaryPreferences: String? = "", // JSON

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
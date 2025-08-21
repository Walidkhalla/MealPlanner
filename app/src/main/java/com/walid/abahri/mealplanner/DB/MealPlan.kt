package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Int, // Associate meal plan with specific user

    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD

    @ColumnInfo(name = "meal_type")
    val mealType: String, // breakfast, lunch, dinner, snack

    @ColumnInfo(name = "recipe_id")
    val recipeId: Int,

    @ColumnInfo(name = "servings")
    val servings: Int = 1,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Int, // Associate recipe with specific user

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    // Note: ingredients are now managed through RecipeIngredient junction table

    @ColumnInfo(name = "instructions")
    val instructions: String,

    @ColumnInfo(name = "prep_time_minutes")
    val prepTimeMinutes: Int,

    @ColumnInfo(name = "cook_time_minutes")
    val cookTimeMinutes: Int,

    @ColumnInfo(name = "servings")
    val servings: Int,

    @ColumnInfo(name = "calories_per_serving")
    val caloriesPerServing: Int?,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "difficulty_level")
    val difficultyLevel: String, // Easy, Medium, Hard

    @ColumnInfo(name = "image_url")
    val imageUrl: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "rating")
    val rating: Float = 0f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions for Recipe
fun Recipe.getTotalTimeMinutes(): Int = prepTimeMinutes + cookTimeMinutes
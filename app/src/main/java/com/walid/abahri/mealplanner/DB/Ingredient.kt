package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an ingredient with nutritional information
 */
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String, // Vegetables, Fruits, Proteins, Grains, Dairy, etc.

    @ColumnInfo(name = "default_unit")
    val defaultUnit: String, // g, ml, cup, piece, etc.

    // Nutritional information per 100g/100ml
    @ColumnInfo(name = "calories_per_100g")
    val caloriesPer100g: Float = 0f,

    @ColumnInfo(name = "protein_per_100g")
    val proteinPer100g: Float = 0f, // in grams

    @ColumnInfo(name = "carbs_per_100g")
    val carbsPer100g: Float = 0f, // in grams

    @ColumnInfo(name = "fat_per_100g")
    val fatPer100g: Float = 0f, // in grams

    @ColumnInfo(name = "fiber_per_100g")
    val fiberPer100g: Float = 0f, // in grams

    @ColumnInfo(name = "sugar_per_100g")
    val sugarPer100g: Float = 0f, // in grams

    @ColumnInfo(name = "sodium_per_100g")
    val sodiumPer100g: Float = 0f, // in mg

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for ingredient with amount (used in recipes and grocery lists)
 */
data class IngredientWithAmount(
    val ingredient: Ingredient,
    val amount: Float,
    val unit: String
) {
    /**
     * Calculate nutritional values for this specific amount
     */
    fun calculateNutrition(): NutritionInfo {
        val factor = convertToGrams() / 100f
        return NutritionInfo(
            calories = ingredient.caloriesPer100g * factor,
            protein = ingredient.proteinPer100g * factor,
            carbs = ingredient.carbsPer100g * factor,
            fat = ingredient.fatPer100g * factor,
            fiber = ingredient.fiberPer100g * factor,
            sugar = ingredient.sugarPer100g * factor,
            sodium = ingredient.sodiumPer100g * factor
        )
    }

    /**
     * Convert amount to grams for nutrition calculation
     * This is a simplified conversion - in a real app you'd have a comprehensive unit conversion system
     */
    private fun convertToGrams(): Float {
        return when (unit.lowercase()) {
            "g", "gram", "grams" -> amount
            "kg", "kilogram", "kilograms" -> amount * 1000
            "ml", "milliliter", "milliliters" -> amount // Assuming 1ml = 1g for liquids
            "l", "liter", "liters" -> amount * 1000
            "cup", "cups" -> amount * 240 // Approximate
            "tbsp", "tablespoon", "tablespoons" -> amount * 15
            "tsp", "teaspoon", "teaspoons" -> amount * 5
            "piece", "pieces", "item", "items" -> amount * 100 // Default assumption
            else -> amount * 100 // Default fallback
        }
    }
}

/**
 * Data class to hold calculated nutrition information
 */
data class NutritionInfo(
    val calories: Float = 0f,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f
) {
    operator fun plus(other: NutritionInfo): NutritionInfo {
        return NutritionInfo(
            calories = this.calories + other.calories,
            protein = this.protein + other.protein,
            carbs = this.carbs + other.carbs,
            fat = this.fat + other.fat,
            fiber = this.fiber + other.fiber,
            sugar = this.sugar + other.sugar,
            sodium = this.sodium + other.sodium
        )
    }

    operator fun times(multiplier: Float): NutritionInfo {
        return NutritionInfo(
            calories = this.calories * multiplier,
            protein = this.protein * multiplier,
            carbs = this.carbs * multiplier,
            fat = this.fat * multiplier,
            fiber = this.fiber * multiplier,
            sugar = this.sugar * multiplier,
            sodium = this.sodium * multiplier
        )
    }
}

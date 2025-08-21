package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing user's daily nutrition goals
 */
@Entity(tableName = "nutrition_goals")
data class NutritionGoal(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Int, // One nutrition goal per user

    @ColumnInfo(name = "daily_calories")
    val dailyCalories: Float = 2000f,

    @ColumnInfo(name = "daily_protein")
    val dailyProtein: Float = 150f, // in grams

    @ColumnInfo(name = "daily_carbs")
    val dailyCarbs: Float = 250f, // in grams

    @ColumnInfo(name = "daily_fat")
    val dailyFat: Float = 65f, // in grams

    @ColumnInfo(name = "daily_fiber")
    val dailyFiber: Float = 25f, // in grams

    @ColumnInfo(name = "daily_sugar_limit")
    val dailySugarLimit: Float = 50f, // in grams

    @ColumnInfo(name = "daily_sodium_limit")
    val dailySodiumLimit: Float = 2300f, // in mg

    @ColumnInfo(name = "activity_level")
    val activityLevel: String = "moderate", // sedentary, light, moderate, active, very_active

    @ColumnInfo(name = "goal_type")
    val goalType: String = "maintain", // lose_weight, maintain, gain_weight, gain_muscle

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for tracking daily nutrition progress
 */
data class DailyNutritionProgress(
    val date: String, // YYYY-MM-DD format
    val goals: NutritionGoal,
    val consumed: NutritionInfo,
    val remaining: NutritionInfo = NutritionInfo(
        calories = maxOf(0f, goals.dailyCalories - consumed.calories),
        protein = maxOf(0f, goals.dailyProtein - consumed.protein),
        carbs = maxOf(0f, goals.dailyCarbs - consumed.carbs),
        fat = maxOf(0f, goals.dailyFat - consumed.fat),
        fiber = maxOf(0f, goals.dailyFiber - consumed.fiber),
        sugar = maxOf(0f, goals.dailySugarLimit - consumed.sugar),
        sodium = maxOf(0f, goals.dailySodiumLimit - consumed.sodium)
    )
) {
    /**
     * Calculate percentage of goals achieved
     */
    fun getCaloriesPercentage(): Float = (consumed.calories / goals.dailyCalories * 100).coerceAtMost(100f)
    fun getProteinPercentage(): Float = (consumed.protein / goals.dailyProtein * 100).coerceAtMost(100f)
    fun getCarbsPercentage(): Float = (consumed.carbs / goals.dailyCarbs * 100).coerceAtMost(100f)
    fun getFatPercentage(): Float = (consumed.fat / goals.dailyFat * 100).coerceAtMost(100f)
    fun getFiberPercentage(): Float = (consumed.fiber / goals.dailyFiber * 100).coerceAtMost(100f)

    /**
     * Check if user exceeded limits
     */
    fun isSugarExceeded(): Boolean = consumed.sugar > goals.dailySugarLimit
    fun isSodiumExceeded(): Boolean = consumed.sodium > goals.dailySodiumLimit
    fun isCaloriesExceeded(): Boolean = consumed.calories > goals.dailyCalories

    /**
     * Get overall goal achievement status
     */
    fun getOverallStatus(): NutritionStatus {
        val caloriesPercentage = getCaloriesPercentage()
        return when {
            caloriesPercentage < 80f -> NutritionStatus.UNDER_TARGET
            caloriesPercentage > 110f -> NutritionStatus.OVER_TARGET
            else -> NutritionStatus.ON_TARGET
        }
    }
}

enum class NutritionStatus {
    UNDER_TARGET,
    ON_TARGET,
    OVER_TARGET
}

package com.walid.abahri.mealplanner.DB

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("DELETE FROM meal_plans")
    suspend fun deleteAll()

    // Get all meal plans for a specific user
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId ORDER BY date, meal_type")
    fun getAllMealPlans(userId: Int): LiveData<List<MealPlan>>
    
    // Get meal plans for a specific date and user
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date ORDER BY meal_type")
    fun getMealPlansForDate(userId: Int, date: String): LiveData<List<MealPlan>>

    // Get meal plans for a date range and user (non-LiveData for synchronous access)
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date, meal_type")
    suspend fun getMealPlansInRange(userId: Int, startDate: String, endDate: String): List<MealPlan>
    
    // Get weekly meal plans with LiveData for a specific user
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date, meal_type")
    fun getMealPlansForWeek(userId: Int, startDate: String, endDate: String): LiveData<List<MealPlan>>

    // Insert a meal plan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(mealPlan: MealPlan)

    // Delete a meal plan
    @Delete
    suspend fun deleteMeal(mealPlan: MealPlan)

    // Delete all meal plans for a specific date and user
    @Query("DELETE FROM meal_plans WHERE user_id = :userId AND date = :date")
    suspend fun deleteMealPlansForDate(userId: Int, date: String)
    
    // Delete meal by date, type and user
    @Query("DELETE FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType")
    suspend fun deleteMealByDateAndType(userId: Int, date: String, mealType: String)
    
    // Update servings for a meal plan, ensuring it belongs to the user
    @Query("UPDATE meal_plans SET servings = :servings WHERE id = :mealPlanId AND user_id = :userId")
    suspend fun updateMealPlanServings(mealPlanId: Int, userId: Int, servings: Int)
    
    // Get the total count of meal plans for a specific user (for profile statistics)
    @Query("SELECT COUNT(*) FROM meal_plans WHERE user_id = :userId")
    suspend fun getMealPlanCount(userId: Int): Int

    // Additional methods for nutrition tracking

    /**
     * Get meal plans for a specific date as Flow
     */
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date ORDER BY meal_type")
    fun getMealPlansForDate(userId: Int, date: String): Flow<List<MealPlan>>

    /**
     * Get meal plans for a date range as Flow
     */
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date, meal_type")
    fun getMealPlansForDateRange(userId: Int, startDate: String, endDate: String): Flow<List<MealPlan>>
}
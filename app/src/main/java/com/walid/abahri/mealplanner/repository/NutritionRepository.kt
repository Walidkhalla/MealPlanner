package com.walid.abahri.mealplanner.repository

import android.content.Context
import androidx.annotation.WorkerThread
import com.walid.abahri.mealplanner.DB.*
import com.walid.abahri.mealplanner.util.UserManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NutritionRepository(
    private val nutritionGoalDao: NutritionGoalDao,
    private val mealPlanDao: MealPlanDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val context: Context
) {
    
    private val userManager = UserManager.getInstance(context)

    fun getNutritionGoal(): Flow<NutritionGoal?> {
        val userId = userManager.getCurrentUserId()
        return nutritionGoalDao.getNutritionGoalByUserId(userId)
    }

    @WorkerThread
    suspend fun insertOrUpdateNutritionGoal(nutritionGoal: NutritionGoal) {
        val userId = userManager.getCurrentUserId()
        val goalWithUserId = nutritionGoal.copy(
            userId = userId,
            updatedAt = System.currentTimeMillis()
        )
        nutritionGoalDao.insertNutritionGoal(goalWithUserId)
    }

    @WorkerThread
    suspend fun deleteNutritionGoal() {
        val userId = userManager.getCurrentUserId()
        nutritionGoalDao.deleteNutritionGoalByUserId(userId)
    }

    @WorkerThread
    suspend fun hasNutritionGoal(): Boolean {
        val userId = userManager.getCurrentUserId()
        return nutritionGoalDao.hasNutritionGoal(userId) > 0
    }

    /**
     * Calculate daily nutrition progress for a specific date
     */
    fun getDailyNutritionProgress(date: String): Flow<DailyNutritionProgress?> {
        val userId = userManager.getCurrentUserId()
        
        return combine(
            getNutritionGoal(),
            mealPlanDao.getMealPlansForDate(userId, date)
        ) { nutritionGoal, mealPlans ->
            if (nutritionGoal == null) return@combine null
            
            // Calculate total nutrition consumed from all meals
            var totalNutrition = NutritionInfo()
            
            for (mealPlan in mealPlans) {
                val recipeNutrition = calculateRecipeNutrition(mealPlan.recipeId, mealPlan.servings)
                totalNutrition = totalNutrition + recipeNutrition
            }
            
            DailyNutritionProgress(
                date = date,
                goals = nutritionGoal,
                consumed = totalNutrition
            )
        }
    }

    /**
     * Calculate nutrition for a specific recipe and serving size
     */
    suspend fun calculateRecipeNutrition(recipeId: Int, servings: Int = 1): NutritionInfo {
        // This would need to be implemented with proper database queries
        // For now, return empty nutrition info
        return NutritionInfo()
    }

    /**
     * Get weekly nutrition summary
     */
    fun getWeeklyNutritionSummary(startDate: String, endDate: String): Flow<List<DailyNutritionProgress>> {
        val userId = userManager.getCurrentUserId()
        
        return combine(
            getNutritionGoal(),
            mealPlanDao.getMealPlansForDateRange(userId, startDate, endDate)
        ) { nutritionGoal, mealPlans ->
            if (nutritionGoal == null) return@combine emptyList()
            
            // Group meal plans by date and calculate daily nutrition
            val dailyProgress = mutableListOf<DailyNutritionProgress>()
            val mealsByDate = mealPlans.groupBy { it.date }
            
            for ((date, meals) in mealsByDate) {
                var dailyNutrition = NutritionInfo()
                
                for (meal in meals) {
                    val recipeNutrition = calculateRecipeNutrition(meal.recipeId, meal.servings)
                    dailyNutrition = dailyNutrition + recipeNutrition
                }
                
                dailyProgress.add(
                    DailyNutritionProgress(
                        date = date,
                        goals = nutritionGoal,
                        consumed = dailyNutrition
                    )
                )
            }
            
            dailyProgress.sortedBy { it.date }
        }
    }
}

package com.walid.abahri.mealplanner.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.walid.abahri.mealplanner.DB.MealPlan
import com.walid.abahri.mealplanner.DB.MealPlanDao
import com.walid.abahri.mealplanner.util.UserManager

class MealPlanRepository(private val mealPlanDao: MealPlanDao, private val context: Context) {
    private val userManager = UserManager.getInstance(context)
    val allMealPlans: LiveData<List<MealPlan>> = mealPlanDao.getAllMealPlans(userManager.getCurrentUserId())

    // Get all meal plans for a given user
    fun getAllMealPlans(userId: Int): LiveData<List<MealPlan>> {
        return mealPlanDao.getAllMealPlans(userId)
    }

    // Get meal plans for a specific date (legacy)
    fun getMealPlansForDate(date: String): LiveData<List<MealPlan>> {
        val userId = userManager.getCurrentUserId()
        return mealPlanDao.getMealPlansForDate(userId, date)
    }

    // Get meal plans for a specific date (new, with userId)
    fun getMealPlansForDate(userId: Int, date: String): LiveData<List<MealPlan>> {
        return mealPlanDao.getMealPlansForDate(userId, date)
    }
    
    // Get meal plans within a date range (for grocery list generation)
    suspend fun getMealPlansInRange(startDate: String, endDate: String): List<MealPlan> {
        val userId = userManager.getCurrentUserId()
        return mealPlanDao.getMealPlansInRange(userId, startDate, endDate)
    }
    
    // Clear all meal plans for a specific date
    suspend fun clearMealPlansForDate(date: String) {
        val userId = userManager.getCurrentUserId()
        mealPlanDao.deleteMealPlansForDate(userId, date)
    }
    
    // Update servings for a meal plan
    suspend fun updateMealPlanServings(mealPlanId: Int, servings: Int) {
        val userId = userManager.getCurrentUserId()
        mealPlanDao.updateMealPlanServings(mealPlanId, userId, servings)
    }

    // Basic operations
    suspend fun insertMeal(plan: MealPlan) {
        val userId = userManager.getCurrentUserId()
        // Make sure the meal plan is associated with the current user
        val planWithUserId = plan.copy(userId = userId)
        mealPlanDao.insertMeal(planWithUserId)
    }
    
    suspend fun deleteMeal(plan: MealPlan) {
        val userId = userManager.getCurrentUserId()
        // Only delete if it belongs to the current user
        if (plan.userId == userId) {
            mealPlanDao.deleteMeal(plan)
        }
    }
}

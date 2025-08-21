package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.GroceryItem
import com.walid.abahri.mealplanner.DB.MealPlan
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.repository.GroceryRepository
import com.walid.abahri.mealplanner.repository.MealPlanRepository
import com.walid.abahri.mealplanner.repository.RecipeRepository
import com.walid.abahri.mealplanner.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val mealPlanRepository: MealPlanRepository
    private val recipeRepository: RecipeRepository
    private val groceryRepository: GroceryRepository
    private val userManager: UserManager
    private val userId: Int
        get() = userManager.getCurrentUserId()
    val allMealPlans: LiveData<List<MealPlan>>

    init {
        val database = AppDatabase.getDatabase(application)
        userManager = UserManager.getInstance(application)
        mealPlanRepository = MealPlanRepository(database.mealPlanDao(), application)
        recipeRepository = RecipeRepository(database.recipeDao(), application)
        groceryRepository = GroceryRepository(database.groceryItemDao(), application)
        allMealPlans = mealPlanRepository.getAllMealPlans(userId)
    }

    // Get meal plans for specific date
    fun getMealPlansForDate(date: String): LiveData<List<MealPlan>> {
        return mealPlanRepository.getMealPlansForDate(userId, date)
    }
    
    // Get recipe by ID (used to display recipe details in meal plan)
    fun getRecipeById(recipeId: Int) = recipeRepository.getRecipeById(recipeId).asLiveData()

    // Add a meal plan
    fun addMealPlan(plan: MealPlan) {
        viewModelScope.launch(Dispatchers.IO) {
            mealPlanRepository.insertMeal(plan)
        }
    }

    // Delete a meal plan
    fun deleteMealPlan(plan: MealPlan) {
        viewModelScope.launch(Dispatchers.IO) {
            mealPlanRepository.deleteMeal(plan)
        }
    }
    
    // Clear all meal plans for a specific date
    fun clearMealPlansForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mealPlanRepository.clearMealPlansForDate(date)
        }
    }
    
    // Update meal plan servings
    fun updateMealPlanServings(mealPlanId: Int, servings: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mealPlanRepository.updateMealPlanServings(mealPlanId, servings)
        }
    }
    
    // Generate grocery list from meal plans in date range
    fun generateGroceryList(startDate: String, endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get all meal plans within the date range
            val mealPlans = mealPlanRepository.getMealPlansInRange(startDate, endDate)
            
            // Generate placeholder grocery items based on meal plans
            // In a production app, you would use real recipe data from the database
            val groceryItems = mutableListOf<GroceryItem>()
            
            // Create sample grocery items for each meal plan
            mealPlans.forEach { mealPlan ->
                // Create placeholder ingredients for this meal
                val ingredients = listOf(
                    Triple("Chicken", "200", "g"),
                    Triple("Rice", "1", "cup"),
                    Triple("Broccoli", "2", "cups"),
                    Triple("Olive Oil", "1", "tbsp"),
                    Triple("Salt", "1", "tsp")
                )
                
                // Create grocery items from these ingredients
                val userId = userManager.getCurrentUserId()
                ingredients.forEach { (name, amountStr, unit) ->
                    val amount = amountStr.toFloatOrNull()?.times(mealPlan.servings) ?: 1f
                    val groceryItem = GroceryItem(
                        name = name,
                        amount = amount,
                        unit = unit,
                        category = "Other",
                        isChecked = false,
                        userId = userId
                    )
                    groceryItems.add(groceryItem)
                }
            }
            
            // Merge similar items
            val mergedItems = mergeGroceryItems(groceryItems)
            
            // Clear existing grocery list and add new items
            groceryRepository.clearAllItems()
            
            // Use the bulk insert method for better performance
            groceryRepository.insertItems(mergedItems)
        }
    }
    
    // Helper function to merge similar grocery items
    private fun mergeGroceryItems(items: List<GroceryItem>): List<GroceryItem> {
        val result = mutableMapOf<String, GroceryItem>()
        
        items.forEach { item ->
            val key = "${item.name}_${item.unit}"
            if (result.containsKey(key)) {
                // If the item already exists, add the amounts
                val existingItem = result[key]!!
                val newAmount = existingItem.amount + item.amount
                result[key] = existingItem.copy(amount = newAmount)
            } else {
                // If the item doesn't exist, add it to the map
                result[key] = item
            }
        }
        
        return result.values.toList()
    }
}

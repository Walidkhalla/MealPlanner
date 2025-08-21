package com.walid.abahri.mealplanner.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.DB.RecipeDao
import com.walid.abahri.mealplanner.util.UserManager
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao, private val context: Context) {
    
    private val userManager = UserManager.getInstance(context)

    // Get all recipes
    fun getAllRecipes(): Flow<List<Recipe>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getAllRecipes(userId)
    }

    // Get recipes by category
    fun getRecipesByCategory(category: String): Flow<List<Recipe>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getRecipesByCategory(userId, category)
    }

    // Get favorite recipes
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getFavoriteRecipes(userId)
    }

    // Get recipe by ID
    fun getRecipeById(id: Int): Flow<Recipe?> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getRecipeById(id, userId)
    }

    // Search recipes
    fun searchRecipes(query: String): Flow<List<Recipe>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.searchRecipes(userId, query)
    }

    // Filter recipes by category and cooking time
    fun getRecipesByCategoryAndTime(category: String, maxTimeMinutes: Int): Flow<List<Recipe>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getRecipesByCategoryAndTime(userId, category, maxTimeMinutes)
    }

    // Get all categories
    fun getAllCategories(): Flow<List<String>> {
        val userId = userManager.getCurrentUserId()
        return recipeDao.getAllCategories(userId)
    }

    // Insert recipe
    @WorkerThread
    suspend fun insertRecipe(recipe: Recipe): Long {
        val userId = userManager.getCurrentUserId()
        // Make sure the recipe is associated with the current user
        val recipeWithUserId = recipe.copy(userId = userId)
        return recipeDao.insertRecipe(recipeWithUserId)
    }

    // Update recipe
    @WorkerThread
    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe.copy(updatedAt = System.currentTimeMillis()))
    }

    // Delete recipe
    @WorkerThread
    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    // Toggle favorite status
    @WorkerThread
    suspend fun toggleFavorite(recipeId: Int, isFavorite: Boolean) {
        val userId = userManager.getCurrentUserId()
        recipeDao.updateFavoriteStatus(recipeId, userId, isFavorite)
    }

    // Update rating
    @WorkerThread
    suspend fun updateRating(recipeId: Int, rating: Float) {
        val userId = userManager.getCurrentUserId()
        recipeDao.updateRating(recipeId, userId, rating)
    }

    // Delete recipe by ID
    @WorkerThread
    suspend fun deleteRecipeById(recipeId: Int) {
        val userId = userManager.getCurrentUserId()
        recipeDao.deleteRecipeById(recipeId, userId)
    }
}

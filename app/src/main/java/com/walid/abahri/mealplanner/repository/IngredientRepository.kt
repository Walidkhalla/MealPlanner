package com.walid.abahri.mealplanner.repository

import androidx.annotation.WorkerThread
import com.walid.abahri.mealplanner.DB.Ingredient
import com.walid.abahri.mealplanner.DB.IngredientDao
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {

    fun getAllIngredients(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()

    fun getIngredientsByCategory(category: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByCategory(category)

    fun getIngredientById(id: Int): Flow<Ingredient?> = ingredientDao.getIngredientById(id)

    fun searchIngredients(query: String): Flow<List<Ingredient>> = 
        ingredientDao.searchIngredients(query)

    fun getAllCategories(): Flow<List<String>> = ingredientDao.getAllCategories()

    @WorkerThread
    suspend fun insertIngredient(ingredient: Ingredient): Long = 
        ingredientDao.insertIngredient(ingredient)

    @WorkerThread
    suspend fun insertIngredients(ingredients: List<Ingredient>) = 
        ingredientDao.insertIngredients(ingredients)

    @WorkerThread
    suspend fun updateIngredient(ingredient: Ingredient) = 
        ingredientDao.updateIngredient(ingredient.copy(updatedAt = System.currentTimeMillis()))

    @WorkerThread
    suspend fun deleteIngredient(ingredient: Ingredient) = 
        ingredientDao.deleteIngredient(ingredient)

    @WorkerThread
    suspend fun deleteIngredientById(id: Int) = 
        ingredientDao.deleteIngredientById(id)
}

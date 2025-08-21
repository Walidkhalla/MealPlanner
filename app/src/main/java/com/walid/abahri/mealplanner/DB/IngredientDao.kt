package com.walid.abahri.mealplanner.DB

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE category = :category ORDER BY name ASC")
    fun getIngredientsByCategory(category: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    fun getIngredientById(id: Int): Flow<Ingredient?>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchIngredients(query: String): Flow<List<Ingredient>>

    @Query("SELECT DISTINCT category FROM ingredients ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: Int)
}

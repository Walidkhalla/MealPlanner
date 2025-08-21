package com.walid.abahri.mealplanner.DB

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("DELETE FROM recipes")
    suspend fun deleteAll()


    @Query("SELECT * FROM recipes WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllRecipes(userId: Int): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE user_id = :userId AND category = :category ORDER BY title ASC")
    fun getRecipesByCategory(userId: Int, category: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE user_id = :userId AND is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteRecipes(userId: Int): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id AND user_id = :userId")
    fun getRecipeById(id: Int, userId: Int): Flow<Recipe?>

    @Query("""
        SELECT * FROM recipes 
        WHERE user_id = :userId
        AND (title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
           OR instructions LIKE '%' || :query || '%')
        ORDER BY title ASC
    """)
    fun searchRecipes(userId: Int, query: String): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes 
        WHERE user_id = :userId
        AND category = :category 
        AND prep_time_minutes + cook_time_minutes <= :maxTimeMinutes
        ORDER BY title ASC
    """)
    fun getRecipesByCategoryAndTime(userId: Int, category: String, maxTimeMinutes: Int): Flow<List<Recipe>>

    @Query("SELECT DISTINCT category FROM recipes WHERE user_id = :userId ORDER BY category ASC")
    fun getAllCategories(userId: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("UPDATE recipes SET is_favorite = :isFavorite WHERE id = :recipeId AND user_id = :userId")
    suspend fun updateFavoriteStatus(recipeId: Int, userId: Int, isFavorite: Boolean)

    @Query("UPDATE recipes SET rating = :rating WHERE id = :recipeId AND user_id = :userId")
    suspend fun updateRating(recipeId: Int, userId: Int, rating: Float)

    @Query("DELETE FROM recipes WHERE id = :recipeId AND user_id = :userId")
    suspend fun deleteRecipeById(recipeId: Int, userId: Int)
    
    // Get the total count of recipes (for profile statistics)
    @Query("SELECT COUNT(*) FROM recipes WHERE user_id = :userId")
    suspend fun getRecipeCount(userId: Int): Int
}
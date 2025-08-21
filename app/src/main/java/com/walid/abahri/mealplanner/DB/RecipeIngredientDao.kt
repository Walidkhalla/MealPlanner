package com.walid.abahri.mealplanner.DB

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {

    @Query("""
        SELECT * FROM recipe_ingredients 
        WHERE recipe_id = :recipeId
        ORDER BY order_index ASC
    """)
    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithIngredients(recipeId: Int): Flow<RecipeWithIngredients?>


    @Query("SELECT * FROM recipe_ingredients WHERE ingredient_id = :ingredientId")
    fun getRecipesUsingIngredient(ingredientId: Int): Flow<List<RecipeIngredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(recipeIngredients: List<RecipeIngredient>)

    @Update
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Delete
    suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteAllIngredientsForRecipe(recipeId: Int)

    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteRecipeIngredientById(id: Int)

    @Transaction
    suspend fun updateRecipeIngredients(recipeId: Int, ingredients: List<RecipeIngredient>) {
        deleteAllIngredientsForRecipe(recipeId)
        val ingredientsWithRecipeId = ingredients.map { it.copy(recipeId = recipeId) }
        insertRecipeIngredients(ingredientsWithRecipeId)
    }
}

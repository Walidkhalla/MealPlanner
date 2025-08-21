package com.walid.abahri.mealplanner.DB

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionGoalDao {

    @Query("SELECT * FROM nutrition_goals WHERE user_id = :userId")
    fun getNutritionGoalByUserId(userId: Int): Flow<NutritionGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionGoal(nutritionGoal: NutritionGoal)

    @Update
    suspend fun updateNutritionGoal(nutritionGoal: NutritionGoal)

    @Delete
    suspend fun deleteNutritionGoal(nutritionGoal: NutritionGoal)

    @Query("DELETE FROM nutrition_goals WHERE user_id = :userId")
    suspend fun deleteNutritionGoalByUserId(userId: Int)

    @Query("SELECT COUNT(*) FROM nutrition_goals WHERE user_id = :userId")
    suspend fun hasNutritionGoal(userId: Int): Int
}

package com.walid.abahri.mealplanner.DB

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.walid.abahri.mealplanner.DB.*
import com.walid.abahri.mealplanner.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Recipe::class, MealPlan::class, GroceryItem::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MealPlannerDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryDao(): GroceryItemDao

    companion object {
        @Volatile
        private var INSTANCE: MealPlannerDatabase? = null

        fun getDatabase(context: Context): MealPlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealPlannerDatabase::class.java,
                    "meal_planner_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Populate database with initial data
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.recipeDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(recipeDao: RecipeDao) {
            // Default user ID for sample data
            val defaultUserId = 1
            
            // Add some sample recipes
            val sampleRecipes = listOf(
                Recipe(
                    title = "Spaghetti Carbonara",
                    description = "Classic Italian pasta dish with eggs, cheese, and pancetta",
                    ingredients = "Spaghetti:400:g,Pancetta:200:g,Eggs:4:large,Parmesan cheese:100:g,Black pepper:1:tsp",
                    instructions = "1. Cook spaghetti according to package instructions\n2. Fry pancetta until crispy\n3. Mix eggs and cheese\n4. Combine hot pasta with pancetta\n5. Add egg mixture and toss quickly\n6. Season with black pepper and serve",
                    prepTimeMinutes = 10,
                    cookTimeMinutes = 15,
                    servings = 4,
                    caloriesPerServing = 520,
                    category = "Dinner",
                    difficultyLevel = "Medium",
                    imageUrl = null,
                    userId = defaultUserId
                ),
                Recipe(
                    title = "Avocado Toast",
                    description = "Healthy and delicious breakfast with avocado and toppings",
                    ingredients = "Bread:2:slices,Avocado:1:ripe,Lemon juice:1:tbsp,Salt:1:pinch,Cherry tomatoes:5:pieces",
                    instructions = "1. Toast the bread slices\n2. Mash avocado with lemon juice and salt\n3. Spread avocado mixture on toast\n4. Top with sliced cherry tomatoes\n5. Season with additional salt and pepper if desired",
                    prepTimeMinutes = 5,
                    cookTimeMinutes = 2,
                    servings = 1,
                    caloriesPerServing = 280,
                    category = "Breakfast",
                    difficultyLevel = "Easy",
                    imageUrl = null,
                    userId = defaultUserId
                )
            )

            sampleRecipes.forEach { recipe ->
                recipeDao.insertRecipe(recipe)
            }
        }
    }
}

// Type converters for Room database
class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromListString(list: List<String>): String {
        return list.joinToString(",")
    }
}
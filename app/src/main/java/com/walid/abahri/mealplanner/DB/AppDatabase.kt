package com.walid.abahri.mealplanner.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.walid.abahri.mealplanner.DB.GroceryItem

@Database(
    entities = [User::class, Recipe::class, MealPlan::class, GroceryItem::class, Ingredient::class, RecipeIngredient::class, NutritionGoal::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun nutritionGoalDao(): NutritionGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 4 to version 5 - Add new nutrition and ingredient tables
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create ingredients table
                database.execSQL("""
                    CREATE TABLE ingredients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        default_unit TEXT NOT NULL,
                        calories_per_100g REAL NOT NULL DEFAULT 0,
                        protein_per_100g REAL NOT NULL DEFAULT 0,
                        carbs_per_100g REAL NOT NULL DEFAULT 0,
                        fat_per_100g REAL NOT NULL DEFAULT 0,
                        fiber_per_100g REAL NOT NULL DEFAULT 0,
                        sugar_per_100g REAL NOT NULL DEFAULT 0,
                        sodium_per_100g REAL NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)

                // Create recipe_ingredients junction table
                database.execSQL("""
                    CREATE TABLE recipe_ingredients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recipe_id INTEGER NOT NULL,
                        ingredient_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        unit TEXT NOT NULL,
                        notes TEXT,
                        is_optional INTEGER NOT NULL DEFAULT 0,
                        order_index INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
                        FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE CASCADE
                    )
                """)

                // Create nutrition_goals table
                database.execSQL("""
                    CREATE TABLE nutrition_goals (
                        user_id INTEGER PRIMARY KEY NOT NULL,
                        daily_calories REAL NOT NULL DEFAULT 2000,
                        daily_protein REAL NOT NULL DEFAULT 150,
                        daily_carbs REAL NOT NULL DEFAULT 250,
                        daily_fat REAL NOT NULL DEFAULT 65,
                        daily_fiber REAL NOT NULL DEFAULT 25,
                        daily_sugar_limit REAL NOT NULL DEFAULT 50,
                        daily_sodium_limit REAL NOT NULL DEFAULT 2300,
                        activity_level TEXT NOT NULL DEFAULT 'moderate',
                        goal_type TEXT NOT NULL DEFAULT 'maintain',
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)

                // Insert some basic ingredients
                val currentTime = System.currentTimeMillis()
                val basicIngredients = listOf(
                    "('Chicken Breast', 'Protein', 'g', 165, 31, 0, 3.6, 0, 0, 74, $currentTime, $currentTime)",
                    "('Rice', 'Grains', 'g', 130, 2.7, 28, 0.3, 0.4, 0.1, 5, $currentTime, $currentTime)",
                    "('Broccoli', 'Vegetables', 'g', 34, 2.8, 7, 0.4, 2.6, 1.5, 33, $currentTime, $currentTime)",
                    "('Tomato', 'Vegetables', 'g', 18, 0.9, 3.9, 0.2, 1.2, 2.6, 5, $currentTime, $currentTime)",
                    "('Onion', 'Vegetables', 'g', 40, 1.1, 9.3, 0.1, 1.7, 4.2, 4, $currentTime, $currentTime)",
                    "('Olive Oil', 'Fats', 'ml', 884, 0, 0, 100, 0, 0, 2, $currentTime, $currentTime)",
                    "('Eggs', 'Protein', 'piece', 155, 13, 1.1, 11, 0, 1.1, 124, $currentTime, $currentTime)",
                    "('Milk', 'Dairy', 'ml', 42, 3.4, 5, 1, 0, 5, 44, $currentTime, $currentTime)",
                    "('Bread', 'Grains', 'slice', 265, 9, 49, 3.2, 2.7, 5, 491, $currentTime, $currentTime)",
                    "('Avocado', 'Fruits', 'piece', 160, 2, 9, 15, 7, 0.7, 7, $currentTime, $currentTime)"
                )

                basicIngredients.forEach { ingredient ->
                    database.execSQL("""
                        INSERT INTO ingredients (name, category, default_unit, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, fiber_per_100g, sugar_per_100g, sodium_per_100g, created_at, updated_at)
                        VALUES $ingredient
                    """)
                }
            }
        }

        // Migration from version 3 to version 4 - Add user_id to all tables
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Get the current logged in user ID, default to 1 for existing data
                val currentUserId = 1 // Default to user ID 1 for existing data
                
                // Add user_id column to recipes table
                database.execSQL("ALTER TABLE recipes ADD COLUMN user_id INTEGER NOT NULL DEFAULT $currentUserId")
                
                // Add user_id column to meal_plans table
                database.execSQL("ALTER TABLE meal_plans ADD COLUMN user_id INTEGER NOT NULL DEFAULT $currentUserId")
                
                // Add user_id column to grocery_items table
                database.execSQL("ALTER TABLE grocery_items ADD COLUMN user_id INTEGER NOT NULL DEFAULT $currentUserId")
            }
        }

        // Migration from version 2 to version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the full_name column to users table
                database.execSQL("ALTER TABLE users ADD COLUMN full_name TEXT NOT NULL DEFAULT ''")
            }
        }
        
        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create any tables that don't exist
                // Users table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS users_new ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "username TEXT NOT NULL, "
                    + "password_hash TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL, "
                    + "daily_calorie_goal INTEGER, "
                    + "dietary_preferences TEXT)"
                )
                
                // Try to migrate data if possible from old schema to new schema
                try {
                    database.execSQL(
                        "INSERT OR IGNORE INTO users_new (id, username, password_hash, email, created_at) "
                        + "SELECT userId, username, password, '', " + System.currentTimeMillis() + " FROM users"
                    )
                    // Drop the old table
                    database.execSQL("DROP TABLE IF EXISTS users")
                    // Rename the new table to the correct name
                    database.execSQL("ALTER TABLE users_new RENAME TO users")
                } catch (e: Exception) {
                    // If the old table doesn't exist or has a different schema, just create the new table
                    database.execSQL("DROP TABLE IF EXISTS users_new")
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS users ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "username TEXT NOT NULL, "
                        + "password_hash TEXT NOT NULL, "
                        + "email TEXT NOT NULL, "
                        + "created_at INTEGER NOT NULL, "
                        + "daily_calorie_goal INTEGER, "
                        + "dietary_preferences TEXT)"
                    )
                }
                
                // --- Start of recipes table migration (safer strategy) ---
                // 1. Rename existing 'recipes' table to 'recipes_old' if it exists.
                var recipesOldExists = false
                try {
                    database.execSQL("ALTER TABLE recipes RENAME TO recipes_old")
                    recipesOldExists = true // If rename succeeded, old table existed.
                } catch (e: Exception) {
                    // Assuming error means 'recipes' table did not exist.
                    // android.util.Log.i("Migration", "Original 'recipes' table not found, will create new one.");
                }
 
                // 2. Create the new 'recipes' table with the correct schema (description TEXT - nullable)
                database.execSQL(
                    "CREATE TABLE recipes (" // Not "IF NOT EXISTS" - we want this exact schema
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "user_id INTEGER NOT NULL DEFAULT 1, " // Add user_id with default value
                    + "title TEXT NOT NULL, "
                    + "description TEXT, " // Nullable
                    + "ingredients TEXT NOT NULL, "
                    + "instructions TEXT NOT NULL, "
                    + "prep_time_minutes INTEGER NOT NULL, "
                    + "cook_time_minutes INTEGER NOT NULL, "
                    + "servings INTEGER NOT NULL, "
                    + "calories_per_serving INTEGER, "
                    + "category TEXT NOT NULL, "
                    + "difficulty_level TEXT NOT NULL, "
                    + "image_url TEXT, "
                    + "is_favorite INTEGER NOT NULL DEFAULT 0, "
                    + "rating REAL NOT NULL DEFAULT 0, "
                    + "created_at INTEGER NOT NULL, "
                    + "updated_at INTEGER NOT NULL)"
                )
 
                // 3. If 'recipes_old' exists, copy data from it to the new 'recipes' table.
                if (recipesOldExists) {
                    try {
                        database.execSQL(
                            "INSERT INTO recipes (id, user_id, title, description, ingredients, instructions, prep_time_minutes, cook_time_minutes, servings, calories_per_serving, category, difficulty_level, image_url, is_favorite, rating, created_at, updated_at) "
                            + "SELECT id, 1, title, description, ingredients, instructions, prep_time_minutes, cook_time_minutes, servings, calories_per_serving, category, difficulty_level, image_url, is_favorite, rating, created_at, updated_at FROM recipes_old"
                        )
                    } catch (e: Exception) {
                        // Data copy failed. New 'recipes' table will be empty.
                        // android.util.Log.e("Migration", "Failed to copy data from recipes_old to recipes: " + e.message);
                    }
                    
                    // 4. Drop 'recipes_old' table.
                    database.execSQL("DROP TABLE recipes_old")
                }
                // --- End of recipes table migration ---
 
                // Migration for 'meal_plans' table
                database.execSQL("ALTER TABLE meal_plans RENAME TO meal_plans_old_temp_migration")

                database.execSQL("""
                    CREATE TABLE meal_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL DEFAULT 1,
                        date TEXT NOT NULL,
                        meal_type TEXT NOT NULL,
                        recipe_id INTEGER NOT NULL,
                        servings INTEGER NOT NULL,
                        notes TEXT,
                        created_at INTEGER NOT NULL
                    )
                """)

                val mealPlanMigrationTime = System.currentTimeMillis()
                database.execSQL("""
                    INSERT INTO meal_plans (id, user_id, date, meal_type, recipe_id, servings, notes, created_at)
                    SELECT planId, 1, day, 'Unknown', recipeId, 1, NULL, $mealPlanMigrationTime
                    FROM meal_plans_old_temp_migration
                """)

                database.execSQL("DROP TABLE meal_plans_old_temp_migration")

                // Migration for 'grocery_items' table
                database.execSQL("ALTER TABLE grocery_items RENAME TO grocery_items_old_temp")

                database.execSQL("""
                    CREATE TABLE grocery_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL DEFAULT 1,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        unit TEXT NOT NULL,
                        category TEXT NOT NULL,
                        is_checked INTEGER NOT NULL DEFAULT 0,
                        added_date INTEGER NOT NULL,
                        recipe_source_id INTEGER
                    )
                """)

                val groceryMigrationTime = System.currentTimeMillis()
                try {
                    // Copy data from old table to new table
                    // Convert 'acquired' to 'is_checked', parse 'quantity' to extract amount and unit if possible
                    database.execSQL("""
                        INSERT INTO grocery_items (id, user_id, name, amount, unit, category, is_checked, added_date, recipe_source_id)
                        SELECT 
                            itemId, 
                            1, 
                            name, 
                            CAST(quantity AS REAL), 
                            'unit', 
                            'Other', 
                            acquired, 
                            $groceryMigrationTime, 
                            NULL
                        FROM grocery_items_old_temp
                    """)
                } catch (e: Exception) {
                    // In case of error during data migration, we'll still have the new table structure
                    // Log error if needed
                }

                database.execSQL("DROP TABLE grocery_items_old_temp")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove ingredients column from recipes table since we now use RecipeIngredient junction table
                database.execSQL("ALTER TABLE recipes RENAME TO recipes_old_temp")

                database.execSQL("""
                    CREATE TABLE recipes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        instructions TEXT NOT NULL,
                        prep_time_minutes INTEGER NOT NULL,
                        cook_time_minutes INTEGER NOT NULL,
                        servings INTEGER NOT NULL,
                        calories_per_serving INTEGER,
                        category TEXT NOT NULL,
                        difficulty_level TEXT NOT NULL,
                        image_url TEXT,
                        is_favorite INTEGER NOT NULL DEFAULT 0,
                        rating REAL NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)

                // Copy data from old table to new table (excluding ingredients column)
                database.execSQL("""
                    INSERT INTO recipes (id, user_id, title, description, instructions, prep_time_minutes, cook_time_minutes, servings, calories_per_serving, category, difficulty_level, image_url, is_favorite, rating, created_at, updated_at)
                    SELECT id, user_id, title, description, instructions, prep_time_minutes, cook_time_minutes, servings, calories_per_serving, category, difficulty_level, image_url, is_favorite, rating, created_at, updated_at
                    FROM recipes_old_temp
                """)

                database.execSQL("DROP TABLE recipes_old_temp")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if exists (to avoid multiple instances)
            return INSTANCE ?: synchronized(this) {
                // Build the database if not already created
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meal_planner_db"
                )
                // First attempt with migration
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // Add the migration strategies
                // Fallback to destructive migration if needed
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
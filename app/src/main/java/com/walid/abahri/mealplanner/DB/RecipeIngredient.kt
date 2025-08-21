package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Junction table linking recipes to ingredients with amounts
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipe_id"]),
        Index(value = ["ingredient_id"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "recipe_id")
    val recipeId: Int,

    @ColumnInfo(name = "ingredient_id")
    val ingredientId: Int,

    @ColumnInfo(name = "amount")
    val amount: Float,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null, // Optional preparation notes like "chopped", "diced", etc.

    @ColumnInfo(name = "is_optional")
    val isOptional: Boolean = false,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0 // For maintaining ingredient order in recipe
)

/**
 * Data class combining recipe ingredient with actual ingredient details
 */
data class RecipeIngredientWithDetails(
    val recipeIngredient: RecipeIngredient,
    val ingredient: Ingredient
) {
    /**
     * Get formatted display string for the ingredient
     */
    fun getDisplayText(): String {
        val amountText = if (recipeIngredient.amount % 1.0f == 0.0f) {
            recipeIngredient.amount.toInt().toString()
        } else {
            recipeIngredient.amount.toString()
        }
        
        val baseText = "$amountText ${recipeIngredient.unit} ${ingredient.name}"
        
        return if (recipeIngredient.notes?.isNotBlank() == true) {
            "$baseText (${recipeIngredient.notes})"
        } else {
            baseText
        }
    }

    /**
     * Calculate nutrition for this ingredient amount
     */
    fun calculateNutrition(): NutritionInfo {
        val ingredientWithAmount = IngredientWithAmount(
            ingredient = ingredient,
            amount = recipeIngredient.amount,
            unit = recipeIngredient.unit
        )
        return ingredientWithAmount.calculateNutrition()
    }

    /**
     * Convert to grocery list item
     */
    fun toGroceryListItem(userId: Int): GroceryItem {
        return GroceryItem(
            userId = userId,
            name = ingredient.name,
            amount = recipeIngredient.amount,
            unit = recipeIngredient.unit,
            category = ingredient.category,
            isChecked = false,
            recipeSourceId = recipeIngredient.recipeId
        )
    }
}

/**
 * Data class for recipe with all its ingredients
 */
data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val recipeIngredients: List<RecipeIngredient>
) {
    // Note: These methods would need ingredient details from a separate query
    // For now, they are simplified to work with RecipeIngredient only
    
    /**
     * Get all recipe ingredients as grocery list items
     * Note: This simplified version doesn't include ingredient category
     */
    fun toGroceryListItems(userId: Int, servings: Int = recipe.servings): List<GroceryItem> {
        val multiplier = if (recipe.servings > 0) servings.toFloat() / recipe.servings else 1f
        
        return recipeIngredients.map { recipeIngredient ->
            GroceryItem(
                userId = userId,
                name = "Ingredient ${recipeIngredient.ingredientId}", // Would need ingredient name from separate query
                amount = recipeIngredient.amount * multiplier,
                unit = recipeIngredient.unit,
                category = "Unknown", // Would need ingredient category from separate query
                isChecked = false,
                recipeSourceId = recipe.id
            )
        }
    }
}

package com.walid.abahri.mealplanner.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a grocery item in the database
 */
@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Int, // Associate grocery item with specific user

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Float = 1f,

    @ColumnInfo(name = "unit")
    val unit: String = "",

    @ColumnInfo(name = "category")
    val category: String = "Other",

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,

    @ColumnInfo(name = "added_date")
    val addedDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "recipe_source_id")
    val recipeSourceId: Int? = null
)
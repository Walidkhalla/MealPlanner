package com.walid.abahri.mealplanner.util

import android.util.Log
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.GroceryItem

object DebugUtils {
    fun logAllGroceryItems(context: android.content.Context) {
        val db = AppDatabase.getDatabase(context)
        val items: List<GroceryItem> = db.groceryItemDao().getAllItemsRaw()
        Log.d("DebugUtils", "--- ALL GROCERY ITEMS ---")
        for (item in items) {
            Log.d("DebugUtils", "ID: ${item.id}, Name: ${item.name}, userId: ${item.userId}")
        }
        Log.d("DebugUtils", "--- END ---")
    }
}

package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.GroceryItem
import com.walid.abahri.mealplanner.UI.GroceryListFragment
import com.walid.abahri.mealplanner.repository.GroceryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GroceryRepository
    private val userManager = com.walid.abahri.mealplanner.util.UserManager.getInstance(getApplication())
    private val userId: Int
        get() = userManager.getCurrentUserId()
    val allItems: LiveData<List<GroceryItem>>
    
    // Cache for filtered items
    private val filteredItemsCache = MutableLiveData<List<GroceryItem>>()

    init {
        val groceryDao = AppDatabase.getDatabase(application).groceryItemDao()
        repository = GroceryRepository(groceryDao, application)
        allItems = repository.getAllItems(userId)
    }
    
    /**
     * Get filtered grocery items based on search query, category, and sort order
     */
    fun getFilteredItems(
        searchQuery: String,
        category: String?,
        sortOrder: GroceryListFragment.SortOrder
    ): List<GroceryItem> {
        val items = allItems.value ?: listOf()
        
        // Apply filters
        val filtered = items.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesCategory = if (category == null) {
                true
            } else {
                item.category == category
            }
            
            matchesSearch && matchesCategory
        }
        
        // Apply sorting
        val sorted = when (sortOrder) {
            GroceryListFragment.SortOrder.NAME -> filtered.sortedBy { it.name }
            GroceryListFragment.SortOrder.CATEGORY -> filtered.sortedWith(
                compareBy({ it.category }, { it.name })
            )
        }
        
        // Update cache
        filteredItemsCache.postValue(sorted)
        
        return sorted
    }
    
    /**
     * Add a new grocery item
     */
    fun addItem(item: GroceryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertItem(item)
        }
    }

    /**
     * Update an existing grocery item
     */
    fun updateItem(item: GroceryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item)
        }
    }
    
    /**
     * Update just the checked status of an item
     */
    fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItemCheckedStatus(itemId, isChecked)
        }
    }

    /**
     * Delete a grocery item
     */
    fun deleteItem(item: GroceryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(item)
        }
    }
    
    /**
     * Clear all completed (checked) items
     */
    fun clearCompletedItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCompletedItems()
        }
    }
    
    /**
     * Clear all grocery items
     */
    fun clearAllItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllItems()
        }
    }
}

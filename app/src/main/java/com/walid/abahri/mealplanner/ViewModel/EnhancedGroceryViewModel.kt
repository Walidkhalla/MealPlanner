package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.*
import com.walid.abahri.mealplanner.repository.GroceryRepository
import com.walid.abahri.mealplanner.service.GroceryListService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EnhancedGroceryViewModel(
    application: Application,
    private val groceryRepository: GroceryRepository,
    private val groceryListService: GroceryListService
) : AndroidViewModel(application) {

    // All grocery items
    val allGroceryItems = groceryRepository.getAllGroceryItems().asLiveData()

    // Grocery items organized by category
    private val _groceryItemsByCategory = MutableStateFlow<Map<String, List<GroceryItem>>>(emptyMap())
    val groceryItemsByCategory: StateFlow<Map<String, List<GroceryItem>>> = _groceryItemsByCategory.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Auto-generation status
    private val _isAutoGenerating = MutableStateFlow(false)
    val isAutoGenerating: StateFlow<Boolean> = _isAutoGenerating.asStateFlow()

    init {
        // Load grocery items by category on initialization
        loadGroceryItemsByCategory()
    }

    /**
     * Generate grocery list from meal plans for current week
     */
    fun generateWeeklyGroceryList(weekStartDate: String) = viewModelScope.launch {
        try {
            _isAutoGenerating.value = true
            _errorMessage.value = null

            val generatedItems = groceryListService.generateWeeklyGroceryList(weekStartDate)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to generate grocery list: ${e.message}"
        } finally {
            _isAutoGenerating.value = false
        }
    }

    /**
     * Generate grocery list from meal plans for date range
     */
    fun generateGroceryListForDateRange(startDate: String, endDate: String) = viewModelScope.launch {
        try {
            _isAutoGenerating.value = true
            _errorMessage.value = null

            val generatedItems = groceryListService.generateGroceryListFromMealPlans(
                startDate, endDate, clearExisting = true
            )
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to generate grocery list: ${e.message}"
        } finally {
            _isAutoGenerating.value = false
        }
    }

    /**
     * Add ingredients from a specific recipe to grocery list
     */
    fun addRecipeToGroceryList(recipeId: Int, servings: Int = 1) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryListService.addRecipeIngredientsToGroceryList(recipeId, servings)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to add recipe ingredients: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Consolidate similar grocery items
     */
    fun consolidateGroceryList() = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryListService.consolidateGroceryList()
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to consolidate grocery list: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Mark multiple ingredients as purchased
     */
    fun markIngredientsPurchased(ingredientNames: List<String>) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryListService.markIngredientsPurchased(ingredientNames)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to mark ingredients as purchased: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Add a manual grocery item
     */
    fun addManualGroceryItem(
        name: String,
        amount: Float,
        unit: String,
        category: String = "Other"
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            val groceryItem = GroceryItem(
                userId = 0, // Will be set by repository
                name = name,
                amount = amount,
                unit = unit,
                category = category,
                isChecked = false
            )

            groceryRepository.insertGroceryItem(groceryItem)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to add grocery item: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Update grocery item
     */
    fun updateGroceryItem(item: GroceryItem) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryRepository.updateGroceryItem(item)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to update grocery item: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Delete grocery item
     */
    fun deleteGroceryItem(item: GroceryItem) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryRepository.deleteGroceryItem(item)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete grocery item: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Toggle grocery item checked status
     */
    fun toggleGroceryItemChecked(item: GroceryItem) = viewModelScope.launch {
        try {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            groceryRepository.updateGroceryItem(updatedItem)
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to update grocery item: ${e.message}"
        }
    }

    /**
     * Clear all completed (checked) items
     */
    fun clearCompletedItems() = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryRepository.clearCompletedItems()
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to clear completed items: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Clear all auto-generated items
     */
    fun clearAutoGeneratedItems() = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            groceryRepository.clearAutoGeneratedItems()
            
            // Refresh the categorized view
            loadGroceryItemsByCategory()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to clear auto-generated items: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Load grocery items organized by category
     */
    private fun loadGroceryItemsByCategory() = viewModelScope.launch {
        try {
            val itemsByCategory = groceryListService.getGroceryListByCategory()
            _groceryItemsByCategory.value = itemsByCategory
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load grocery items: ${e.message}"
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Get shopping progress (percentage of items checked)
     */
    fun getShoppingProgress(): StateFlow<Float> {
        val progressFlow = MutableStateFlow(0f)
        
        viewModelScope.launch {
            allGroceryItems.observeForever { items ->
                if (items.isNotEmpty()) {
                    val checkedCount = items.count { it.isChecked }
                    val progress = (checkedCount.toFloat() / items.size) * 100f
                    progressFlow.value = progress
                }
            }
        }
        
        return progressFlow.asStateFlow()
    }
}

/**
 * ViewModelFactory for EnhancedGroceryViewModel
 */
class EnhancedGroceryViewModelFactory(
    private val application: Application,
    private val groceryRepository: GroceryRepository,
    private val groceryListService: GroceryListService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedGroceryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancedGroceryViewModel(application, groceryRepository, groceryListService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

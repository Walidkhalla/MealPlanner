package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.Ingredient
import com.walid.abahri.mealplanner.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class IngredientViewModel(
    application: Application,
    private val repository: IngredientRepository
) : AndroidViewModel(application) {

    // All ingredients
    val allIngredients = repository.getAllIngredients().asLiveData()
    
    // All categories
    val allCategories = repository.getAllCategories().asLiveData()

    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered ingredients based on search and category
    val filteredIngredients = searchQuery.flatMapLatest { query ->
        if (query.isNotBlank()) {
            repository.searchIngredients(query)
        } else {
            selectedCategory.flatMapLatest { category ->
                if (category == "All") {
                    repository.getAllIngredients()
                } else {
                    repository.getIngredientsByCategory(category)
                }
            }
        }
    }.asLiveData()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Insert a new ingredient
     */
    fun insertIngredient(ingredient: Ingredient) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            repository.insertIngredient(ingredient)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to add ingredient: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Update an existing ingredient
     */
    fun updateIngredient(ingredient: Ingredient) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            repository.updateIngredient(ingredient)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update ingredient: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Delete an ingredient
     */
    fun deleteIngredient(ingredient: Ingredient) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            repository.deleteIngredient(ingredient)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete ingredient: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Set search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Set selected category filter
     */
    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
    }

    /**
     * Get ingredient by ID
     */
    fun getIngredientById(id: Int) = repository.getIngredientById(id).asLiveData()

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

/**
 * ViewModelFactory for IngredientViewModel
 */
class IngredientViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            val ingredientDao = AppDatabase.getDatabase(application).ingredientDao()
            val repository = IngredientRepository(ingredientDao)
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

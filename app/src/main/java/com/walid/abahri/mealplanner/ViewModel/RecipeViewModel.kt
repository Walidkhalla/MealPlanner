package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application, private val repository: RecipeRepository) : AndroidViewModel(application) {

    // LiveData for recipes list, always filtered by current user
    private val userManager = com.walid.abahri.mealplanner.util.UserManager.getInstance(getApplication())
    private val userId: Int
        get() = userManager.getCurrentUserId()

    val allRecipes = repository.getAllRecipes().asLiveData()
    val favoriteRecipes = repository.getFavoriteRecipes().asLiveData()
    val allCategories = repository.getAllCategories().asLiveData()

    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _maxCookingTime = MutableStateFlow(120) // 2 hours default
    val maxCookingTime = _maxCookingTime.asStateFlow()

    // We've replaced this with the more comprehensive filteredRecipesWithFavorites below

    // Insert new recipe
    fun insertRecipe(recipe: Recipe) = viewModelScope.launch {
        repository.insertRecipe(recipe)
    }

    // Update existing recipe
    fun updateRecipe(recipe: Recipe) = viewModelScope.launch {
        repository.updateRecipe(recipe)
    }

    // Delete recipe
    fun deleteRecipe(recipe: Recipe) = viewModelScope.launch {
        repository.deleteRecipe(recipe)
    }

    // Toggle favorite status
    fun toggleFavorite(recipeId: Int, isFavorite: Boolean) = viewModelScope.launch {
        repository.toggleFavorite(recipeId, isFavorite)
    }

    // Update recipe rating
    fun updateRating(recipeId: Int, rating: Float) = viewModelScope.launch {
        repository.updateRating(recipeId, rating)
    }

    // Search functions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setMaxCookingTime(minutes: Int) {
        _maxCookingTime.value = minutes
    }

    // Clear all filters
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        _maxCookingTime.value = 120
    }
    
    // Toggle favorites filter
    private val _showingFavorites = MutableStateFlow(false)
    val showingFavorites = _showingFavorites.asStateFlow()
    
    // Modified filtered recipes to include the favorites filter
    val filteredRecipesWithFavorites = combine(
        searchQuery,
        selectedCategory,
        maxCookingTime,
        showingFavorites
    ) { query, category, maxTime, onlyFavorites ->
        FilterParams(query, category, maxTime, onlyFavorites)
    }.flatMapLatest { params ->
        val uid = userId
        when {
            params.onlyFavorites -> repository.getFavoriteRecipes()
            params.query.isNotBlank() -> repository.searchRecipes(params.query)
            params.category != "All" -> repository.getRecipesByCategoryAndTime(params.category, params.maxTime)
            else -> repository.getAllRecipes()
        }
    }.asLiveData()
    
    // Data class to hold filter parameters
    private data class FilterParams(
        val query: String,
        val category: String,
        val maxTime: Int,
        val onlyFavorites: Boolean
    )
    
    fun toggleFavoritesFilter() {
        _showingFavorites.value = !_showingFavorites.value
    }

    // Get recipe by ID
    fun getRecipeById(id: Int) = repository.getRecipeById(id).asLiveData()
}

// ViewModelFactory for RecipeViewModel
class RecipeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            val recipeDao = AppDatabase.getDatabase(application).recipeDao()
            val repository = RecipeRepository(recipeDao, application)
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

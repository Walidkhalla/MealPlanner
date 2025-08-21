package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.*
import com.walid.abahri.mealplanner.repository.RecipeRepository
import com.walid.abahri.mealplanner.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class EnhancedRecipeViewModel(
    application: Application,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeIngredientDao: RecipeIngredientDao
) : AndroidViewModel(application) {

    // All recipes with nutrition information
    val allRecipes = recipeRepository.getAllRecipes().asLiveData()
    val favoriteRecipes = recipeRepository.getFavoriteRecipes().asLiveData()
    val allCategories = recipeRepository.getAllCategories().asLiveData()

    // All ingredients for recipe creation
    val allIngredients = ingredientRepository.getAllIngredients().asLiveData()

    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _maxCookingTime = MutableStateFlow(120)
    val maxCookingTime = _maxCookingTime.asStateFlow()

    private val _showingFavorites = MutableStateFlow(false)
    val showingFavorites = _showingFavorites.asStateFlow()

    // Filtered recipes with nutrition
    val filteredRecipes = combine(
        searchQuery,
        selectedCategory,
        maxCookingTime,
        showingFavorites
    ) { query, category, maxTime, onlyFavorites ->
        FilterParams(query, category, maxTime, onlyFavorites)
    }.flatMapLatest { params ->
        when {
            params.onlyFavorites -> recipeRepository.getFavoriteRecipes()
            params.query.isNotBlank() -> recipeRepository.searchRecipes(params.query)
            params.category != "All" -> recipeRepository.getRecipesByCategoryAndTime(params.category, params.maxTime)
            else -> recipeRepository.getAllRecipes()
        }
    }.asLiveData()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Recipe creation/editing state
    private val _selectedRecipeIngredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    val selectedRecipeIngredients: StateFlow<List<RecipeIngredient>> = _selectedRecipeIngredients.asStateFlow()

    private val _calculatedNutrition = MutableStateFlow<NutritionInfo?>(null)
    val calculatedNutrition: StateFlow<NutritionInfo?> = _calculatedNutrition.asStateFlow()

    /**
     * Create a new recipe with ingredients
     */
    fun createRecipeWithIngredients(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            // Insert the recipe first
            val recipeId = recipeRepository.insertRecipe(recipe)
            
            // Then insert the ingredients with the recipe ID
            val ingredientsWithRecipeId = ingredients.map { 
                it.copy(recipeId = recipeId.toInt()) 
            }
            recipeIngredientDao.insertRecipeIngredients(ingredientsWithRecipeId)

        } catch (e: Exception) {
            _errorMessage.value = "Failed to create recipe: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Update recipe with ingredients
     */
    fun updateRecipeWithIngredients(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            // Update the recipe
            recipeRepository.updateRecipe(recipe)
            
            // Update the ingredients (this will replace all existing ingredients)
            recipeIngredientDao.updateRecipeIngredients(recipe.id, ingredients)

        } catch (e: Exception) {
            _errorMessage.value = "Failed to update recipe: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Add ingredient to current recipe being created/edited
     */
    fun addIngredientToRecipe(
        ingredientId: Int,
        amount: Float,
        unit: String,
        notes: String? = null,
        isOptional: Boolean = false
    ) {
        val currentIngredients = _selectedRecipeIngredients.value.toMutableList()
        val orderIndex = currentIngredients.size
        
        val recipeIngredient = RecipeIngredient(
            recipeId = 0, // Will be set when recipe is saved
            ingredientId = ingredientId,
            amount = amount,
            unit = unit,
            notes = notes,
            isOptional = isOptional,
            orderIndex = orderIndex
        )
        
        currentIngredients.add(recipeIngredient)
        _selectedRecipeIngredients.value = currentIngredients
        
        // Recalculate nutrition
        calculateRecipeNutrition()
    }

    /**
     * Remove ingredient from current recipe
     */
    fun removeIngredientFromRecipe(index: Int) {
        val currentIngredients = _selectedRecipeIngredients.value.toMutableList()
        if (index in currentIngredients.indices) {
            currentIngredients.removeAt(index)
            // Reorder the remaining ingredients
            currentIngredients.forEachIndexed { newIndex, ingredient ->
                currentIngredients[newIndex] = ingredient.copy(orderIndex = newIndex)
            }
            _selectedRecipeIngredients.value = currentIngredients
            
            // Recalculate nutrition
            calculateRecipeNutrition()
        }
    }

    /**
     * Update ingredient in current recipe
     */
    fun updateIngredientInRecipe(
        index: Int,
        amount: Float,
        unit: String,
        notes: String? = null,
        isOptional: Boolean = false
    ) {
        val currentIngredients = _selectedRecipeIngredients.value.toMutableList()
        if (index in currentIngredients.indices) {
            val updatedIngredient = currentIngredients[index].copy(
                amount = amount,
                unit = unit,
                notes = notes,
                isOptional = isOptional
            )
            currentIngredients[index] = updatedIngredient
            _selectedRecipeIngredients.value = currentIngredients
            
            // Recalculate nutrition
            calculateRecipeNutrition()
        }
    }

    /**
     * Calculate nutrition for current recipe ingredients
     */
    private fun calculateRecipeNutrition() = viewModelScope.launch {
        try {
            var totalNutrition = NutritionInfo()
            
            for (recipeIngredient in _selectedRecipeIngredients.value) {
                // Get the ingredient details
                val ingredient = ingredientRepository.getIngredientById(recipeIngredient.ingredientId)
                ingredient.asLiveData().observeForever { ing ->
                    if (ing != null) {
                        val ingredientWithAmount = IngredientWithAmount(
                            ingredient = ing,
                            amount = recipeIngredient.amount,
                            unit = recipeIngredient.unit
                        )
                        totalNutrition = totalNutrition + ingredientWithAmount.calculateNutrition()
                    }
                }
            }
            
            _calculatedNutrition.value = totalNutrition
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to calculate nutrition: ${e.message}"
        }
    }

    /**
     * Get recipe with ingredients by ID
     */
    fun getRecipeWithIngredients(recipeId: Int) = recipeIngredientDao.getRecipeWithIngredients(recipeId).asLiveData()

    /**
     * Load recipe for editing
     */
    fun loadRecipeForEditing(recipeId: Int) = viewModelScope.launch {
        try {
            val recipeIngredients = recipeIngredientDao.getRecipeIngredients(recipeId)
            recipeIngredients.asLiveData().observeForever { ingredients ->
                _selectedRecipeIngredients.value = ingredients
                calculateRecipeNutrition()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load recipe: ${e.message}"
        }
    }

    /**
     * Clear current recipe editing state
     */
    fun clearRecipeEditingState() {
        _selectedRecipeIngredients.value = emptyList()
        _calculatedNutrition.value = null
    }

    // Standard recipe operations
    fun insertRecipe(recipe: Recipe) = viewModelScope.launch {
        try {
            _isLoading.value = true
            recipeRepository.insertRecipe(recipe)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to insert recipe: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun updateRecipe(recipe: Recipe) = viewModelScope.launch {
        try {
            _isLoading.value = true
            recipeRepository.updateRecipe(recipe)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update recipe: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteRecipe(recipe: Recipe) = viewModelScope.launch {
        try {
            _isLoading.value = true
            recipeRepository.deleteRecipe(recipe)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete recipe: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleFavorite(recipeId: Int, isFavorite: Boolean) = viewModelScope.launch {
        recipeRepository.toggleFavorite(recipeId, isFavorite)
    }

    fun updateRating(recipeId: Int, rating: Float) = viewModelScope.launch {
        recipeRepository.updateRating(recipeId, rating)
    }

    // Search and filter functions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setMaxCookingTime(minutes: Int) {
        _maxCookingTime.value = minutes
    }

    fun toggleFavoritesFilter() {
        _showingFavorites.value = !_showingFavorites.value
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        _maxCookingTime.value = 120
        _showingFavorites.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getRecipeById(id: Int) = recipeRepository.getRecipeById(id).asLiveData()

    private data class FilterParams(
        val query: String,
        val category: String,
        val maxTime: Int,
        val onlyFavorites: Boolean
    )
}

/**
 * ViewModelFactory for EnhancedRecipeViewModel
 */
class EnhancedRecipeViewModelFactory(
    private val application: Application,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeIngredientDao: RecipeIngredientDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancedRecipeViewModel(application, recipeRepository, ingredientRepository, recipeIngredientDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

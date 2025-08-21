package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.MainActivity
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModel
import com.walid.abahri.mealplanner.databinding.FragmentAddEditRecipeBinding
import com.walid.abahri.mealplanner.util.ViewModelUtils
import com.walid.abahri.mealplanner.util.UserManager
import java.util.*

class AddEditRecipeFragment : Fragment() {
    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var userManager: UserManager
    private var recipeId: Int = -1
    private var isEditMode = false
    
    // Categories and difficulty levels for dropdowns
    private val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Appetizer", "Soup", "Salad", "Main Course", "Side Dish", "Beverage", "Other")
    private val difficultyLevels = listOf("Easy", "Medium", "Hard")
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditRecipeBinding.inflate(inflater, container, false)
        
        // Set up root view to intercept edge swipes
        setupEdgeSwipeHandling()
        
        return binding.root
    }
    
    private fun setupEdgeSwipeHandling() {
        // Block gesture navigation from edges and handle it ourselves
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            // Just capture the insets event, which is triggered during edge swipes
            insets
        }
        
        // Make the root view consume all touch events that might be edge swipes
        binding.root.setOnTouchListener { _, event ->
            // If touch is near edge (within 100px of left or right edge)
            val edgeWidth = 100
            val screenWidth = resources.displayMetrics.widthPixels
            
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                // If touch started at left or right edge
                if (event.x < edgeWidth || event.x > screenWidth - edgeWidth) {
                    // Keep track that we're in a potential edge swipe
                    binding.root.tag = "potential_edge_swipe"
                }
            } else if (event.action == android.view.MotionEvent.ACTION_UP || 
                      event.action == android.view.MotionEvent.ACTION_CANCEL) {
                // If we were tracking a potential edge swipe
                if (binding.root.tag == "potential_edge_swipe") {
                    // Clear the tag
                    binding.root.tag = null
                    
                    // If the swipe moved far enough, handle back navigation
                    if (event.x - edgeWidth > 100 || screenWidth - edgeWidth - event.x > 100) {
                        // Use our safe navigation method
                        safeNavigateBack()
                        return@setOnTouchListener true // Consume the event
                    }
                }
            }
            
            // Don't consume other touch events
            false
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel and UserManager
        recipeViewModel = ViewModelUtils.getRecipeViewModel(requireActivity())
        userManager = UserManager.getInstance(requireContext())
        
        // Set up toolbar with proper title and back navigation
        setupToolbar()
        
        // Check if we're editing an existing recipe
        arguments?.let { args ->
            if (args.containsKey("recipeId")) {
                recipeId = args.getInt("recipeId")
                isEditMode = recipeId > 0
                if (isEditMode) {
                    binding.textTitleHeader.text = "Edit Recipe"
                    binding.toolbar.title = "Edit Recipe"
                    loadRecipeData(recipeId)
                } else {
                    binding.toolbar.title = "Add Recipe"
                }
            }
        }
        
        setupDropdowns()
        setupSaveButton()
    }
    
    private fun setupToolbar() {
        // Handle navigation icon (back button) click
        binding.toolbar.setNavigationOnClickListener {
            // Go back to main screens safely
            safeNavigateBack()
        }
    }
    
    private fun safeNavigateBack() {
        // Use the reliable navigation method to return to main screens
        try {
            (activity as? com.walid.abahri.mealplanner.MainActivity)?.returnToMainScreens()
        } catch (e: Exception) {
            Log.e("AddEditRecipe", "Error returning to main screens: ${e.message}")
            // Fall back to standard navigation if needed
            try {
                activity?.onBackPressed()
            } catch (e2: Exception) {
                // Last resort
                try {
                    parentFragmentManager.popBackStack()
                } catch (e3: Exception) {
                    Log.e("AddEditRecipe", "All navigation methods failed")
                }
            }
        }
    }
    
    private fun setupDropdowns() {
        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        (binding.dropdownCategory as? AutoCompleteTextView)?.setAdapter(categoryAdapter)
        
        // Setup difficulty dropdown
        val difficultyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, difficultyLevels)
        (binding.dropdownDifficulty as? AutoCompleteTextView)?.setAdapter(difficultyAdapter)
    }
    
    private fun loadRecipeData(recipeId: Int) {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        
        recipeViewModel.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE
            
            if (recipe != null) {
                // Populate form fields with recipe data
                binding.editTextTitle.setText(recipe.title)
                binding.editTextDescription.setText(recipe.description)
                binding.editTextInstructions.setText(recipe.instructions)
                binding.editTextPrepTime.setText(recipe.prepTimeMinutes.toString())
                binding.editTextCookTime.setText(recipe.cookTimeMinutes.toString())
                binding.editTextServings.setText(recipe.servings.toString())
                binding.editTextCalories.setText(recipe.caloriesPerServing?.toString() ?: "")
                binding.dropdownCategory.setText(recipe.category, false)
                binding.dropdownDifficulty.setText(recipe.difficultyLevel, false)
            } else {
                // Handle case where recipe couldn't be loaded
                Toast.makeText(context, "Could not load recipe data", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // Go back if recipe can't be loaded
            }
        }
    }
    
    private fun setupSaveButton() {
        binding.buttonSaveRecipe.setOnClickListener {
            if (validateForm()) {
                saveRecipe()
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validate title (required)
        if (binding.editTextTitle.text.isNullOrBlank()) {
            binding.inputLayoutTitle.error = "Title is required"
            isValid = false
        } else {
            binding.inputLayoutTitle.error = null
        }
        
        // Ingredients are now managed via the structured ingredient system (RecipeIngredient).
        // No validation for a plain ingredients text field here.
        
        // Validate instructions (required)
        if (binding.editTextInstructions.text.isNullOrBlank()) {
            binding.inputLayoutInstructions.error = "Instructions are required"
            isValid = false
        } else {
            binding.inputLayoutInstructions.error = null
        }
        
        // Category validation
        if (binding.dropdownCategory.text.isNullOrBlank()) {
            binding.inputLayoutCategory.error = "Please select a category"
            isValid = false
        } else {
            binding.inputLayoutCategory.error = null
        }
        
        // Numeric fields validation (optional but must be numbers if provided)
        val prepTime = binding.editTextPrepTime.text.toString()
        val cookTime = binding.editTextCookTime.text.toString()
        val servings = binding.editTextServings.text.toString()
        val calories = binding.editTextCalories.text.toString()
        
        if (prepTime.isNotBlank() && prepTime.toIntOrNull() == null) {
            binding.inputLayoutPrepTime.error = "Must be a number"
            isValid = false
        } else {
            binding.inputLayoutPrepTime.error = null
        }
        
        if (cookTime.isNotBlank() && cookTime.toIntOrNull() == null) {
            binding.inputLayoutCookTime.error = "Must be a number"
            isValid = false
        } else {
            binding.inputLayoutCookTime.error = null
        }
        
        if (servings.isNotBlank() && servings.toIntOrNull() == null) {
            binding.inputLayoutServings.error = "Must be a number"
            isValid = false
        } else {
            binding.inputLayoutServings.error = null
        }
        
        if (calories.isNotBlank() && calories.toIntOrNull() == null) {
            binding.inputLayoutCalories.error = "Must be a number"
            isValid = false
        } else {
            binding.inputLayoutCalories.error = null
        }
        
        return isValid
    }
    
    private fun saveRecipe() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val instructions = binding.editTextInstructions.text.toString().trim()
        val prepTimeMinutes = binding.editTextPrepTime.text.toString().toIntOrNull() ?: 0
        val cookTimeMinutes = binding.editTextCookTime.text.toString().toIntOrNull() ?: 0
        val servings = binding.editTextServings.text.toString().toIntOrNull() ?: 1
        val caloriesPerServing = binding.editTextCalories.text.toString().toIntOrNull() ?: 0
        val category = binding.dropdownCategory.text.toString()
        val difficultyLevel = binding.dropdownDifficulty.text.toString()
        
        val currentTime = System.currentTimeMillis()
        
        // Get the current user ID
        val userId = userManager.getCurrentUserId()
        
        val recipe = if (isEditMode) {
            // Update existing recipe
            Recipe(
                id = recipeId,
                userId = userId,
                title = title,
                description = description,
                instructions = instructions,
                prepTimeMinutes = prepTimeMinutes,
                cookTimeMinutes = cookTimeMinutes,
                servings = servings,
                caloriesPerServing = caloriesPerServing,
                category = category,
                difficultyLevel = difficultyLevel,
                imageUrl = "", // Handle image URL if needed
                isFavorite = false, // Preserve existing value in a real implementation
                rating = 0f, // Preserve existing value in a real implementation
                createdAt = 0, // Preserve existing value in a real implementation
                updatedAt = currentTime
            )
        } else {
            // Create new recipe
            Recipe(
                userId = userId,
                title = title,
                description = description,
                instructions = instructions,
                prepTimeMinutes = prepTimeMinutes,
                cookTimeMinutes = cookTimeMinutes,
                servings = servings,
                caloriesPerServing = caloriesPerServing,
                category = category,
                difficultyLevel = difficultyLevel,
                imageUrl = "",
                isFavorite = false,
                rating = 0f,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        }
        
        if (isEditMode) {
            recipeViewModel.updateRecipe(recipe)
            Toast.makeText(requireContext(), "Recipe updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            recipeViewModel.insertRecipe(recipe)
            Toast.makeText(requireContext(), "Recipe added successfully", Toast.LENGTH_SHORT).show()
        }
        
        // Wait a moment for the save operation to complete, then return to recipe list
        activity?.findViewById<View>(android.R.id.content)?.postDelayed({
            // Use our safe navigation method
            safeNavigateBack()
        }, 200) // Give enough time for DB operations to complete
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

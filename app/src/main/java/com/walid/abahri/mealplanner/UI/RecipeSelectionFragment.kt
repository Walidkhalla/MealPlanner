package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.MealPlan
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.ViewModel.MealPlanViewModel
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModel
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModelFactory
import com.walid.abahri.mealplanner.util.UserManager
import com.walid.abahri.mealplanner.databinding.FragmentRecipeSelectionBinding
import com.walid.abahri.mealplanner.repository.RecipeRepository
import com.walid.abahri.mealplanner.util.ViewModelUtils
import java.text.SimpleDateFormat
import java.util.*

class RecipeSelectionFragment : Fragment() {
    private var _binding: FragmentRecipeSelectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var mealPlanViewModel: MealPlanViewModel
    private lateinit var userManager: UserManager
    private lateinit var recipeAdapter: RecipeSelectionAdapter
    
    private var selectedRecipe: Recipe? = null
    private var servingsCount = 1
    
    // Arguments from navigation
    private lateinit var date: String
    private lateinit var mealType: String
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSelectionBinding.inflate(inflater, container, false)
        
        // Set up edge swipe handling
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
    
    private fun setupToolbar() {
        // Set the toolbar title to include the meal type
        binding.toolbar.title = "Add to $mealType"
        
        // Set up the back button
        binding.toolbar.setNavigationOnClickListener {
            safeNavigateBack()
        }
    }

    private fun safeNavigateBack() {
        // Use the reliable navigation method to return to main screens
        try {
            (activity as? com.walid.abahri.mealplanner.MainActivity)?.returnToMainScreens()
        } catch (e: Exception) {
            // Fall back to standard navigation if needed
            try {
                // Use modern navigation approach instead of deprecated onBackPressed()
                findNavController().navigateUp()
            } catch (e2: Exception) {
                // Last resort - try to go back to the previous screen
                try {
                    findNavController().popBackStack()
                } catch (e3: Exception) {
                    // Nothing else we can do
                    Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show()
                    Log.e("RecipeSelectionFragment", "All navigation attempts failed: ${e2.message}")
                }
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize UserManager
        userManager = UserManager.getInstance(requireContext())
        
        // Get arguments from navigation with simplified handling
        try {
            // Extract arguments with defaults
            date = arguments?.getString("date") ?: getCurrentDate()
            mealType = arguments?.getString("mealType") ?: "Breakfast"
            
            // Log the values we're using
            Log.d("RecipeSelectionFragment", "Using date=$date, mealType=$mealType")
        } catch (e: Exception) {
            // Use defaults if there's an error
            date = getCurrentDate()
            mealType = "Breakfast"
            Log.e("RecipeSelectionFragment", "Error processing arguments: ${e.message}")
        }
        
        // Set up toolbar with back button
        setupToolbar()
        
        // Update header text with meal info
        val formattedDate = formatDate(date)
        binding.textMealInfo.text = "For $mealType on $formattedDate"
        
        // Initialize ViewModels with proper application context
        try {
            Log.d("RecipeSelectionFragment", "Initializing ViewModels")
            
            // Use ViewModelUtils to create RecipeViewModel with proper factory
            recipeViewModel = ViewModelUtils.getRecipeViewModel(this)
            
            // Initialize MealPlanViewModel with Application context
            // This was a source of previous issues, so we're being extra careful
            val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            mealPlanViewModel = ViewModelProvider(requireActivity(), factory).get(MealPlanViewModel::class.java)
            
            Log.d("RecipeSelectionFragment", "ViewModels initialized successfully")
        } catch (e: Exception) {
            Log.e("RecipeSelectionFragment", "Error initializing ViewModels: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading recipes. Please try again.", Toast.LENGTH_LONG).show()
        }
        
        setupRecyclerView()
        setupObservers()
        setupSearchView()
        setupCategoryChips()
        setupServingsCounter()
    }
    
    private fun setupRecyclerView() {
        recipeAdapter = RecipeSelectionAdapter { recipe ->
            // Immediately add to meal plan on click
            val currentUserId = userManager.getCurrentUserId()
            val mealPlan = MealPlan(
                date = date,
                mealType = mealType,
                recipeId = recipe.id,
                servings = servingsCount,
                userId = currentUserId
            )
            mealPlanViewModel.addMealPlan(mealPlan)
            Toast.makeText(requireContext(), "${recipe.title} added to $mealType", Toast.LENGTH_SHORT).show()
            safeNavigateBack()
        }
        
        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }
    
    private fun setupObservers() {
        recipeViewModel.filteredRecipesWithFavorites.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            
            // Show/hide empty state
            if (recipes.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewRecipes.visibility = View.GONE
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewRecipes.visibility = View.VISIBLE
            }
        }
        
        // Observe categories for filter chips
        recipeViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }
    }
    
    private fun setupSearchView() {
        binding.searchViewRecipes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { recipeViewModel.setSearchQuery(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { recipeViewModel.setSearchQuery(it) }
                return true
            }
        })
    }
    
    private fun setupCategoryChips(categories: List<String> = emptyList()) {
        // Clear existing chips except "All"
        val chipGroup = binding.chipGroupCategories
        val allChip = chipGroup.findViewById<Chip>(R.id.chip_all)
        chipGroup.removeAllViews()
        chipGroup.addView(allChip)
        
        // Add category chips dynamically
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isCheckedIconVisible = true
                setOnClickListener {
                    recipeViewModel.setSelectedCategory(category)
                }
            }
            chipGroup.addView(chip)
        }
        
        // Set click listener for "All" chip
        allChip.setOnClickListener {
            recipeViewModel.clearFilters()
        }
    }
    
    private fun setupServingsCounter() {
        binding.textServingsCount.text = servingsCount.toString()
        
        binding.buttonDecreaseServings.setOnClickListener {
            if (servingsCount > 1) {
                servingsCount--
                binding.textServingsCount.text = servingsCount.toString()
            }
        }
        
        binding.buttonIncreaseServings.setOnClickListener {
            servingsCount++
            binding.textServingsCount.text = servingsCount.toString()
        }
    }
    
    private fun setupAddToMealPlanButton() {
        binding.buttonAddToMealPlan.setOnClickListener {
            selectedRecipe?.let { recipe ->
                // userManager.getCurrentUserId() is confirmed by lint to be non-null Long
                val currentUserId = userManager.getCurrentUserId()

                val mealPlan = MealPlan(
                    date = date,
                    mealType = mealType,
                    recipeId = recipe.id,
                    servings = servingsCount,
                    userId = currentUserId // Directly use, as it's non-null
                )
                
                Log.d("RecipeSelectionFragment", "Adding to meal plan: UserID: ${mealPlan.userId}, RecipeID: ${mealPlan.recipeId}, Date: ${mealPlan.date}, MealType: ${mealPlan.mealType}")
                mealPlanViewModel.addMealPlan(mealPlan) 
                Toast.makeText(
                    requireContext(),
                    "${recipe.title} added to $mealType",
                    Toast.LENGTH_SHORT
                ).show()
                
                safeNavigateBack()
            } ?: run {
                 // This part is executed if selectedRecipe is null
                 Toast.makeText(requireContext(), "Please select a recipe first.", Toast.LENGTH_SHORT).show()
            }
        }
        // Ensure the button is disabled initially. It's enabled by the adapter when a recipe is selected.
        binding.buttonAddToMealPlan.isEnabled = false
    }
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun formatDate(date: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            return parsedDate?.let { outputFormat.format(it) } ?: date
        } catch (e: Exception) {
            return date
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // RecipeSelectionAdapter
    private inner class RecipeSelectionAdapter(
        private val onRecipeSelected: (Recipe) -> Unit
    ) : RecyclerView.Adapter<RecipeSelectionAdapter.RecipeViewHolder>() {
        
        private var recipes = listOf<Recipe>()
        private var selectedPosition = RecyclerView.NO_POSITION
        
        fun submitList(list: List<Recipe>) {
            recipes = list
            notifyDataSetChanged()
        }
        
        inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleTextView: TextView = itemView.findViewById(R.id.text_recipe_title)
            val categoryTextView: TextView = itemView.findViewById(R.id.text_recipe_category)
            val timeTextView: TextView = itemView.findViewById(R.id.text_recipe_time)
            val caloriesTextView: TextView = itemView.findViewById(R.id.text_recipe_calories)
            
            init {
                itemView.setOnClickListener {
                    val previousSelected = selectedPosition
                    selectedPosition = adapterPosition
                    
                    // Update UI for previously selected item
                    if (previousSelected != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelected)
                    }
                    
                    // Update UI for newly selected item
                    notifyItemChanged(selectedPosition)
                    
                    // Callback to fragment
                    onRecipeSelected(recipes[adapterPosition])
                }
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recipe_selection, parent, false)
            return RecipeViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val recipe = recipes[position]
            
            holder.titleTextView.text = recipe.title
            holder.categoryTextView.text = recipe.category
            holder.timeTextView.text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min"
            holder.caloriesTextView.text = "${recipe.caloriesPerServing} cal"
            
            // Highlight selected item using MaterialCardView's checked state
            (holder.itemView as? com.google.android.material.card.MaterialCardView)?.isChecked = selectedPosition == position
        }
        
        override fun getItemCount() = recipes.size
    }
}

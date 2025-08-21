package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.walid.abahri.mealplanner.MainActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.RecipeAdapter
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModel
import com.walid.abahri.mealplanner.databinding.FragmentRecipeListBinding
import com.walid.abahri.mealplanner.util.ViewModelUtils

class RecipeListFragment : Fragment() {
    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recipeViewModel = ViewModelUtils.getRecipeViewModel(this)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSearchView()
        
        // Setup menu in onViewCreated instead of onCreate to safely use viewLifecycleOwner
        activity?.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.recipe_list_menu, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        showFilterDialog()
                        true
                    }
                    R.id.action_sort -> {
                        showSortDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }
    
    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { _ ->
            },
            onFavoriteClick = { recipe ->
                recipeViewModel.toggleFavorite(recipe.id, !recipe.isFavorite)
            },
            onEditClick = { recipe ->
                try {
                    (activity as? MainActivity)?.navigateToAddEditRecipe(recipe.id)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open recipe editor", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { recipe ->
                showDeleteConfirmationDialog(recipe)
            }
        )
        
        binding.recyclerViewRecipes.adapter = recipeAdapter
        binding.recyclerViewRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
    }
    
    private fun setupObservers() {
        recipeViewModel.filteredRecipesWithFavorites.observe(viewLifecycleOwner) { recipes ->
            updateRecipesList(recipes)
            updateRecipeCount(recipes.size)
        }
        
        binding.btnCategoryAll.isSelected = true
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                recipeViewModel.selectedCategory.collect { category ->
                    updateCategoryFilterButtons(category)
                }
            }
        }
    }
    
    private fun updateRecipesList(recipes: List<Recipe>) {
        recipeAdapter.submitList(recipes)
        
        if (recipes.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewRecipes.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddRecipe.setOnClickListener {
            try {
                (activity as? MainActivity)?.navigateToAddEditRecipe(0)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open recipe creation screen", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.filterButton.setOnClickListener {
            toggleFavoritesFilter()
        }
        
        binding.btnCategoryAll.setOnClickListener {
            selectCategoryButton(binding.btnCategoryAll)
            recipeViewModel.setSelectedCategory("All")
        }
        
        binding.btnCategoryBreakfast.setOnClickListener {
            selectCategoryButton(binding.btnCategoryBreakfast)
            recipeViewModel.setSelectedCategory("Breakfast")
        }
        
        binding.btnCategoryLunch.setOnClickListener {
            selectCategoryButton(binding.btnCategoryLunch)
            recipeViewModel.setSelectedCategory("Lunch")
        }
        
        binding.btnCategoryDinner.setOnClickListener {
            selectCategoryButton(binding.btnCategoryDinner)
            recipeViewModel.setSelectedCategory("Dinner")
        }
        
        binding.btnCategorySnacks.setOnClickListener {
            selectCategoryButton(binding.btnCategorySnacks)
            recipeViewModel.setSelectedCategory("Snacks")
        }
    }
    
    private fun setupSearchView() {
        binding.searchViewRecipes.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
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
    
    private fun updateCategoryFilterButtons(category: String) {
        binding.btnCategoryAll.isSelected = false
        binding.btnCategoryBreakfast.isSelected = false
        binding.btnCategoryLunch.isSelected = false
        binding.btnCategoryDinner.isSelected = false
        binding.btnCategorySnacks.isSelected = false
        
        when (category) {
            "All" -> binding.btnCategoryAll.isSelected = true
            "Breakfast" -> binding.btnCategoryBreakfast.isSelected = true
            "Lunch" -> binding.btnCategoryLunch.isSelected = true
            "Dinner" -> binding.btnCategoryDinner.isSelected = true
            "Snacks" -> binding.btnCategorySnacks.isSelected = true
        }
    }
    
    private fun selectCategoryButton(button: Button) {
        binding.btnCategoryAll.isSelected = false
        binding.btnCategoryBreakfast.isSelected = false
        binding.btnCategoryLunch.isSelected = false
        binding.btnCategoryDinner.isSelected = false
        binding.btnCategorySnacks.isSelected = false
        
        button.isSelected = true
    }
    
    private fun toggleFavoritesFilter() {
        recipeViewModel.toggleFavoritesFilter()
    }
    
    private fun updateRecipeCount(count: Int) {
        binding.recipeCountText.text = "$count recipes found"
    }
    
    private fun showDeleteConfirmationDialog(recipe: Recipe) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete '${recipe.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                recipeViewModel.deleteRecipe(recipe)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menu setup moved to onViewCreated to avoid viewLifecycleOwner access errors
    }
    
    private fun showFilterDialog() {
    }
    
    private fun showSortDialog() {
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

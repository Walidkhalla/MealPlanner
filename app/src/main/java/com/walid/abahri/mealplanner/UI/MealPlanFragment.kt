package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.walid.abahri.mealplanner.DB.MealPlan
import com.walid.abahri.mealplanner.MainActivity
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.ViewModel.MealPlanViewModel
import com.walid.abahri.mealplanner.databinding.FragmentMealPlanBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * MealPlanFragment displays the meal plan for a week and allows users to add, edit, and delete meals.
 * It includes features for calendar navigation, meal type filtering, and nutrition summary.
 */
class MealPlanFragment : Fragment() {
    // View binding for accessing UI elements
    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel for accessing data
    private lateinit var mealPlanViewModel: MealPlanViewModel

    // Calendar instance to manage date navigation
    private val calendar = Calendar.getInstance()
    
    // Date formatters for different display needs
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val weekDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    
    /**
     * Creates the view hierarchy for this fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the view is created. Sets up UI components and loads initial data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize the ViewModel with proper Application context
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        mealPlanViewModel = ViewModelProvider(this, factory).get(MealPlanViewModel::class.java)

        try {
            // Set up UI components
            setupCalendarNavigation()
            setupDayTabs()
            setupMealRecyclerViews()
            setupAddMealButtons()
            setupGroceryListButton()

            // Load initial meal plan data
            loadMealPlanForDate(dateFormatter.format(calendar.time))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error setting up meal plan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Clean up resources when fragment view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCalendarNavigation() {
        updateWeekDisplay()

        binding.buttonPreviousWeek.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            updateWeekDisplay()
            updateDayTabs()
            loadMealPlanForCurrentTab()
        }

        binding.buttonNextWeek.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            updateWeekDisplay()
            updateDayTabs()
            loadMealPlanForCurrentTab()
        }
    }

    private fun updateWeekDisplay() {
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_YEAR, 6)

        val weekText = "${weekDateFormatter.format(startOfWeek.time)} - ${weekDateFormatter.format(endOfWeek.time)}, ${endOfWeek.get(Calendar.YEAR)}"
        binding.textViewCurrentWeek.text = weekText
    }

    private fun setupDayTabs() {
        binding.tabLayoutDays.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val date = tab.tag as String
                loadMealPlanForDate(date)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        updateDayTabs()
    }

    private fun updateDayTabs() {
        // Clear all existing tabs first
        binding.tabLayoutDays.removeAllTabs()

        // Get the first day of the current week
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

        // Get today's date for comparison
        val today = Calendar.getInstance()
        
        // Create tabs for each day of the week
        for (i in 0..6) {
            // Create a calendar for this specific day
            val dayCalendar = startOfWeek.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_YEAR, i)

            // Format the date as needed
            val dateString = dateFormatter.format(dayCalendar.time)
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCalendar.time)
            val dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()

            // Create a tab with day information
            val tab = binding.tabLayoutDays.newTab()
            tab.setText("$dayOfWeek\n$dayOfMonth")
            tab.setTag(dateString)
            binding.tabLayoutDays.addTab(tab)

            // Select the tab if it's today
            if (dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                dayCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                tab.select()
            }
        }

        // If no tab is selected, select the first one
        if (binding.tabLayoutDays.selectedTabPosition == -1) {
            binding.tabLayoutDays.getTabAt(0)?.select()
        }
    }

    private fun setupMealRecyclerViews() {
        // Setup breakfast recycler view
        binding.recyclerViewBreakfast.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBreakfast.adapter = MealAdapter { mealPlan -> 
            showMealOptionsDialog(mealPlan) 
        }

        // Setup lunch recycler view
        binding.recyclerViewLunch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLunch.adapter = MealAdapter { mealPlan -> 
            showMealOptionsDialog(mealPlan) 
        }

        // Setup dinner recycler view
        binding.recyclerViewDinner.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDinner.adapter = MealAdapter { mealPlan -> 
            showMealOptionsDialog(mealPlan) 
        }

        // Setup snacks recycler view
        binding.recyclerViewSnacks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSnacks.adapter = MealAdapter { mealPlan -> 
            showMealOptionsDialog(mealPlan) 
        }
    }

    private fun setupAddMealButtons() {
        binding.buttonAddBreakfast.setOnClickListener {
            val currentSelectedDate = binding.tabLayoutDays.getTabAt(
                binding.tabLayoutDays.selectedTabPosition
            )?.tag as? String ?: dateFormatter.format(calendar.time)
            
            navigateToRecipeSelection(currentSelectedDate, "Breakfast")
        }

        binding.buttonAddLunch.setOnClickListener {
            val currentSelectedDate = binding.tabLayoutDays.getTabAt(
                binding.tabLayoutDays.selectedTabPosition
            )?.tag as? String ?: dateFormatter.format(calendar.time)
            
            navigateToRecipeSelection(currentSelectedDate, "Lunch")
        }

        binding.buttonAddDinner.setOnClickListener {
            val currentSelectedDate = binding.tabLayoutDays.getTabAt(
                binding.tabLayoutDays.selectedTabPosition
            )?.tag as? String ?: dateFormatter.format(calendar.time)
            
            navigateToRecipeSelection(currentSelectedDate, "Dinner")
        }

        binding.buttonAddSnack.setOnClickListener {
            val currentSelectedDate = binding.tabLayoutDays.getTabAt(
                binding.tabLayoutDays.selectedTabPosition
            )?.tag as? String ?: dateFormatter.format(calendar.time)
            
            navigateToRecipeSelection(currentSelectedDate, "Snack")
        }
    }
    
    private fun setupGroceryListButton() {
        try {
            binding.fabGenerateGroceryList.setOnClickListener {
                Toast.makeText(requireContext(), "Generate grocery list feature coming soon", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Silent error handling for optional feature
        }
    }
    
    /**
     * Navigate to recipe selection screen with date and meal type information
     * Using the MainActivity's navigation method for reliable fragment transactions
     */
    private fun navigateToRecipeSelection(date: String, mealType: String) {
        try {
            // Get the current date if date is blank
            val validDate = if (date.isBlank()) dateFormatter.format(calendar.time) else date
            
            // Log navigation attempt for debugging
            Log.d("MealPlanFragment", "Navigating to RecipeSelection with date=$validDate, mealType=$mealType")
            
            // Use the MainActivity's navigation method instead of NavController
            // This uses fragment transactions which are more reliable in this app's structure
            (activity as? MainActivity)?.navigateToRecipeSelection(validDate, mealType) ?: run {
                // Fallback to direct navigation if MainActivity is not available
                try {
                    val bundle = bundleOf(
                        "date" to validDate,
                        "mealType" to mealType
                    )
                    findNavController().navigate(R.id.recipeSelectionFragment, bundle)
                } catch (e: Exception) {
                    Log.e("MealPlanFragment", "Fallback navigation failed: ${e.message}")
                    Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            // Log the error
            Log.e("MealPlanFragment", "Navigation failed: ${e.message}")
            e.printStackTrace()
            
            // Show a simple error message
            Toast.makeText(requireContext(), "Could not open recipe selection. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMealPlanForCurrentTab() {
        val selectedDate = binding.tabLayoutDays.getTabAt(
            binding.tabLayoutDays.selectedTabPosition
        )?.tag as? String

        selectedDate?.let {
            loadMealPlanForDate(it)
        }
    }

    private fun loadMealPlanForDate(date: String) {
        mealPlanViewModel.getMealPlansForDate(date).observe(viewLifecycleOwner) { mealPlans ->
            val breakfastPlans = mealPlans.filter { it.mealType.equals("breakfast", ignoreCase = true) }
            val lunchPlans = mealPlans.filter { it.mealType.equals("lunch", ignoreCase = true) }
            val dinnerPlans = mealPlans.filter { it.mealType.equals("dinner", ignoreCase = true) }
            val snackPlans = mealPlans.filter { it.mealType.equals("snack", ignoreCase = true) }

            (binding.recyclerViewBreakfast.adapter as? MealAdapter)?.submitList(breakfastPlans)
            (binding.recyclerViewLunch.adapter as? MealAdapter)?.submitList(lunchPlans)
            (binding.recyclerViewDinner.adapter as? MealAdapter)?.submitList(dinnerPlans)
            (binding.recyclerViewSnacks.adapter as? MealAdapter)?.submitList(snackPlans)

            updateNutritionSummary(mealPlans)
        }
    }

    private fun updateNutritionSummary(mealPlans: List<MealPlan>) {
        // Simple calculation of total calories
        var totalCalories = 0
        
        // Assuming fixed values for now
        var totalProtein = 0
        var totalCarbs = 0
        var totalFat = 0

        // Calculate calories based on servings
        for (mealPlan in mealPlans) {
            totalCalories += 250 * mealPlan.servings
        }

        try {
            // Update progress bars with calculated values
            binding.progressBarCalories.progress = (totalCalories * 100 / 2000)
            if (binding.progressBarCalories.progress > 100) {
                binding.progressBarCalories.progress = 100
            }
            binding.textViewCaloriesCount.text = "$totalCalories/2000"

            binding.progressBarProtein.progress = (totalProtein * 100 / 75)
            if (binding.progressBarProtein.progress > 100) {
                binding.progressBarProtein.progress = 100
            }
            binding.textViewProteinCount.text = "$totalProtein/75g"

            binding.progressBarCarbs.progress = (totalCarbs * 100 / 250)
            if (binding.progressBarCarbs.progress > 100) {
                binding.progressBarCarbs.progress = 100
            }
            binding.textViewCarbsCount.text = "$totalCarbs/250g"

            binding.progressBarFat.progress = (totalFat * 100 / 70)
            if (binding.progressBarFat.progress > 100) {
                binding.progressBarFat.progress = 100
            }
            binding.textViewFatCount.text = "$totalFat/70g"
        } catch (e: Exception) {
            // Silent error handling
        }
    }

    private fun showMealOptionsDialog(mealPlan: MealPlan) {
        val options = arrayOf("View Recipe", "Edit Servings", "Remove from Plan")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Meal Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        try {
                            findNavController().navigate(
                                R.id.action_mealPlanFragment_to_recipeDetailFragment,
                                Bundle().apply {
                                    putInt("recipeId", mealPlan.recipeId)
                                }
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error navigating to recipe details: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    1 -> {
                        showEditServingsDialog(mealPlan)
                    }
                    2 -> {
                        showDeleteConfirmationDialog(mealPlan)
                    }
                }
            }
            .show()
    }

    private fun showEditServingsDialog(mealPlan: MealPlan) {
        val currentServings = mealPlan.servings
        var newServings = currentServings
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_servings_picker, null)
        val numberPicker = dialogView.findViewById<TextView>(R.id.text_servings_count)
        val decreaseButton = dialogView.findViewById<View>(R.id.button_decrease_servings)
        val increaseButton = dialogView.findViewById<View>(R.id.button_increase_servings)
        
        numberPicker.text = newServings.toString()
        
        decreaseButton.setOnClickListener {
            if (newServings > 1) {
                newServings--
                numberPicker.text = newServings.toString()
            }
        }
        
        increaseButton.setOnClickListener {
            newServings++
            numberPicker.text = newServings.toString()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Servings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (newServings != currentServings) {
                    mealPlanViewModel.updateMealPlanServings(mealPlan.id, newServings)
                    Toast.makeText(context, "Servings updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(mealPlan: MealPlan) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Meal")
            .setMessage("Are you sure you want to remove this meal from your plan?")
            .setPositiveButton("Remove") { _, _ ->
                mealPlanViewModel.deleteMealPlan(mealPlan)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private inner class MealAdapter(private val onItemClick: (MealPlan) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<MealAdapter.MealViewHolder>() {
        private val mealPlans = mutableListOf<MealPlan>()
        
        fun submitList(list: List<MealPlan>) {
            mealPlans.clear()
            mealPlans.addAll(list)
            notifyDataSetChanged()
        }

        inner class MealViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val titleTextView = itemView.findViewById<TextView>(R.id.text_meal_recipe_title)
            val servingsTextView = itemView.findViewById<TextView>(R.id.text_meal_servings)
            val deleteButton = itemView.findViewById<View>(R.id.button_delete_meal)

            init {
                itemView.setOnClickListener {
                    if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        onItemClick(mealPlans[adapterPosition])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
            return MealViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
            val meal = mealPlans[position]
            
            mealPlanViewModel.getRecipeById(meal.recipeId).observe(viewLifecycleOwner) { recipe ->
                holder.titleTextView.text = recipe?.title ?: "Recipe #${meal.recipeId}"
            }
            
            holder.servingsTextView.text = "${meal.servings} serving(s)"
            holder.deleteButton.setOnClickListener { showDeleteConfirmationDialog(meal) }
        }
        
        override fun getItemCount() = mealPlans.size
    }}
package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.walid.abahri.mealplanner.DB.GroceryItem
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.ViewModel.GroceryViewModel
import com.walid.abahri.mealplanner.databinding.FragmentGroceryListBinding
import com.walid.abahri.mealplanner.databinding.DialogAddGroceryItemBinding
import com.walid.abahri.mealplanner.util.UserManager

class GroceryListFragment : Fragment() {
    private var _binding: FragmentGroceryListBinding? = null
    private val binding get() = _binding!!
    private val groceryViewModel: GroceryViewModel by viewModels()
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var userManager: UserManager

    private var currentCategory: String? = null
    private var searchQuery: String = ""
    private var sortOrder: SortOrder = SortOrder.CATEGORY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroceryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userManager = UserManager.getInstance(requireContext())
        setupRecyclerView()
        setupSearch()
        setupCategoryTabs()
        setupAddItemButton()
        setupToolbar()
        observeGroceryItems()
        setupMenu()
    }

    private fun setupRecyclerView() {
        groceryAdapter = GroceryAdapter(
            onItemCheckedChange = { item, isChecked ->
                groceryViewModel.updateItemCheckedStatus(item.id, isChecked)
            },
            onItemEdit = { item ->
                showEditItemDialog(item)
            }
        )
        
        binding.groceryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.groceryRecyclerView.adapter = groceryAdapter
        binding.groceryRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }
    
    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                filterGroceryItems()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.buttonFilter.setOnClickListener {
            showFilterMenu(it)
        }
    }
    
    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.grocery_list_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_sort_category -> {
                    sortOrder = SortOrder.CATEGORY
                    filterGroceryItems()
                    true
                }
                R.id.action_sort_name -> {
                    sortOrder = SortOrder.NAME
                    filterGroceryItems()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun setupCategoryTabs() {
        binding.tabLayoutCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentCategory = if (tab.position == 0) null else tab.text.toString()
                filterGroceryItems()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun setupAddItemButton() {
        binding.fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_clear_completed -> {
                    showClearCompletedConfirmation()
                    true
                }
                R.id.action_clear_all -> {
                    showClearAllConfirmation()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun observeGroceryItems() {
        groceryViewModel.allItems.observe(viewLifecycleOwner) { items ->
            updateCategories(items)
            filterGroceryItems()
            updateProgressBar(items)
        }
    }
    
    private fun updateProgressBar(items: List<GroceryItem>) {
        val checkedItems = items.count { it.isChecked }
        val totalItems = items.size
        
        binding.textViewProgress.text = "Items checked: $checkedItems/$totalItems"
        binding.progressBarItems.max = totalItems
        binding.progressBarItems.progress = checkedItems
        
        binding.textViewEmptyList.isVisible = items.isEmpty()
    }
    
    private fun updateCategories(items: List<GroceryItem>) {
        val categories = items.map { it.category }.distinct().sorted()
        
        val currentTabs = (0 until binding.tabLayoutCategories.tabCount)
            .map { binding.tabLayoutCategories.getTabAt(it)?.text.toString() }
            .drop(1)
        
        if (categories != currentTabs) {
            binding.tabLayoutCategories.removeAllTabs()
            
            binding.tabLayoutCategories.addTab(
                binding.tabLayoutCategories.newTab().setText("All")
            )
            
            categories.forEach { category ->
                binding.tabLayoutCategories.addTab(
                    binding.tabLayoutCategories.newTab().setText(category)
                )
            }
        }
    }
    
    private fun filterGroceryItems() {
        val filteredItems = groceryViewModel.getFilteredItems(searchQuery, currentCategory, sortOrder)
        groceryAdapter.submitList(filteredItems)
    }
    
    private fun showAddItemDialog() {
        val dialogBinding = DialogAddGroceryItemBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Grocery Item")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.editTextItemName.text.toString()
                val amount = dialogBinding.editTextItemAmount.text.toString().toFloatOrNull() ?: 1f
                val unit = dialogBinding.editTextItemUnit.text.toString()
                val category = dialogBinding.editTextItemCategory.text.toString()
                
                if (name.isNotBlank()) {
                    val groceryItem = GroceryItem(
                        name = name,
                        amount = amount,
                        unit = unit,
                        category = category.ifBlank { "Other" },
                        isChecked = false,
                        userId = userManager.getCurrentUserId()
                    )
                    groceryViewModel.addItem(groceryItem)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditItemDialog(item: GroceryItem) {
        val dialogBinding = DialogAddGroceryItemBinding.inflate(layoutInflater)
        
        dialogBinding.editTextItemName.setText(item.name)
        dialogBinding.editTextItemAmount.setText(item.amount.toString())
        dialogBinding.editTextItemUnit.setText(item.unit)
        dialogBinding.editTextItemCategory.setText(item.category)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Grocery Item")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val name = dialogBinding.editTextItemName.text.toString()
                val amount = dialogBinding.editTextItemAmount.text.toString().toFloatOrNull() ?: 1f
                val unit = dialogBinding.editTextItemUnit.text.toString()
                val category = dialogBinding.editTextItemCategory.text.toString()
                
                if (name.isNotBlank()) {
                    val updatedItem = item.copy(
                        name = name,
                        amount = amount,
                        unit = unit,
                        category = category.ifBlank { "Other" }
                    )
                    groceryViewModel.updateItem(updatedItem)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearCompletedConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Completed Items")
            .setMessage("Are you sure you want to remove all checked items from your grocery list?")
            .setPositiveButton("Clear") { _, _ ->
                groceryViewModel.clearCompletedItems()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Items")
            .setMessage("Are you sure you want to remove all items from your grocery list?")
            .setPositiveButton("Clear") { _, _ ->
                groceryViewModel.clearAllItems()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteItemConfirmation(item: GroceryItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '${item.name}' from your grocery list?")
            .setPositiveButton("Delete") { _, _ ->
                groceryViewModel.deleteItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.grocery_list_menu, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
enum class SortOrder {
        NAME, CATEGORY
    }
    
    inner class GroceryAdapter(
        private val onItemCheckedChange: (GroceryItem, Boolean) -> Unit,
        private val onItemEdit: (GroceryItem) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<GroceryItem, GroceryAdapter.GroceryViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<GroceryItem>() {
            override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_grocery, parent, false)
            return GroceryViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
        
        inner class GroceryViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val checkbox = itemView.findViewById<android.widget.CheckBox>(R.id.checkGrocery)
            private val nameTextView = itemView.findViewById<TextView>(R.id.textGroceryItem)
            private val amountTextView = itemView.findViewById<TextView>(R.id.textGroceryAmount)
            private val categoryTextView = itemView.findViewById<TextView>(R.id.textGroceryCategory)
            private val editButton = itemView.findViewById<android.widget.ImageButton>(R.id.buttonEditGrocery)
            private val deleteButton = itemView.findViewById<android.widget.ImageButton>(R.id.buttonDeleteGrocery)
            
            fun bind(item: GroceryItem) {
                nameTextView.text = item.name
                amountTextView.text = "${item.amount} ${item.unit}"
                categoryTextView.text = item.category
                
                if (item.isChecked) {
                    nameTextView.paintFlags = nameTextView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    nameTextView.paintFlags = nameTextView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = item.isChecked
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onItemCheckedChange(item, isChecked)
                }
                
                editButton.setOnClickListener {
                    onItemEdit(item)
                }
                
                deleteButton.setOnClickListener {
                    showDeleteItemConfirmation(item)
                }
            }
        }
    }
}

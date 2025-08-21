package com.walid.abahri.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walid.abahri.mealplanner.DB.MealPlan
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.databinding.ItemMealBinding

/**
 * Adapter for displaying meal items in the meal plan
 */
class MealItemAdapter : ListAdapter<MealPlanWithRecipe, MealItemAdapter.MealItemViewHolder>(MealItemDiffCallback()) {

    private var onItemClick: ((MealPlanWithRecipe) -> Unit)? = null
    private var onDeleteClick: ((MealPlan) -> Unit)? = null

    fun setOnItemClickListener(listener: (MealPlanWithRecipe) -> Unit) {
        onItemClick = listener
    }

    fun setOnDeleteClickListener(listener: (MealPlan) -> Unit) {
        onDeleteClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealItemViewHolder(private val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
            
            binding.buttonDeleteMeal.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick?.invoke(getItem(position).mealPlan)
                }
            }
        }
        
        fun bind(item: MealPlanWithRecipe) {
            binding.textMealRecipeTitle.text = item.recipe?.title ?: "Unknown Recipe"
            binding.textMealServings.text = "${item.mealPlan.servings} serving(s)"
            
            // If there are notes, show them
            if (item.mealPlan.notes.isNullOrEmpty()) {
                binding.textMealNotes.visibility = android.view.View.GONE
            } else {
                binding.textMealNotes.visibility = android.view.View.VISIBLE
                binding.textMealNotes.text = item.mealPlan.notes
            }
        }
    }

    class MealItemDiffCallback : DiffUtil.ItemCallback<MealPlanWithRecipe>() {
        override fun areItemsTheSame(oldItem: MealPlanWithRecipe, newItem: MealPlanWithRecipe): Boolean {
            return oldItem.mealPlan.id == newItem.mealPlan.id
        }

        override fun areContentsTheSame(oldItem: MealPlanWithRecipe, newItem: MealPlanWithRecipe): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class to hold a MealPlan with its associated Recipe
 */
data class MealPlanWithRecipe(
    val mealPlan: MealPlan,
    val recipe: Recipe? = null
)

package com.walid.abahri.mealplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walid.abahri.mealplanner.DB.Recipe
import com.walid.abahri.mealplanner.DB.getTotalTimeMinutes
import com.walid.abahri.mealplanner.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.apply {
                textViewRecipeTitle.text = recipe.title
                textViewRecipeCategory.text = recipe.category
                
                // Use the extension function for total time
                textViewPrepTime.text = "${recipe.getTotalTimeMinutes()} min"
                
                textViewCalories.text = "${recipe.caloriesPerServing ?: 0} cal"
                ratingBarRecipe.rating = recipe.rating

                // Set recipe image
                if (recipe.imageUrl.isNullOrEmpty()) {
                    imageViewRecipe.setImageResource(R.drawable.ic_recipe_placeholder)
                } else {
                    // For now, just use a placeholder
                    // In a real app, you would use Glide or another image loading library
                    imageViewRecipe.setImageResource(R.drawable.ic_recipe_placeholder)
                }

                // Set favorite icon
                imageViewFavorite.setImageResource(
                    if (recipe.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )

                // Click listeners
                root.setOnClickListener { onRecipeClick(recipe) }
                imageViewFavorite.setOnClickListener { onFavoriteClick(recipe) }
                imageViewEdit.setOnClickListener { onEditClick(recipe) }
                imageViewDelete.setOnClickListener { onDeleteClick(recipe) }
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}
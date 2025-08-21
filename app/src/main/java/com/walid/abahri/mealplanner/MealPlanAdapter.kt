package com.walid.abahri.mealplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walid.abahri.mealplanner.DB.MealPlan

class MealPlanAdapter : RecyclerView.Adapter<MealPlanAdapter.MealViewHolder>() {

    private var mealList = listOf<MealPlan>()

    fun submitList(list: List<MealPlan>) {
        mealList = list
        notifyDataSetChanged()
    }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView = itemView.findViewById<TextView>(R.id.textViewMealTitle)
        val servingsTextView = itemView.findViewById<TextView>(R.id.textViewMealServings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_plan, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]
        holder.titleTextView.text = "Recipe ID: ${meal.recipeId}"
        holder.servingsTextView.text = "${meal.servings} serving(s) - ${meal.mealType}"
        // Optional: you can enhance this by fetching the actual recipe name
    }

    override fun getItemCount(): Int = mealList.size
}

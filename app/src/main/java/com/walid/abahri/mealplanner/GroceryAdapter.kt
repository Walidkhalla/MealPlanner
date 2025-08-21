package com.walid.abahri.mealplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walid.abahri.mealplanner.DB.GroceryItem

class GroceryAdapter(
    private val onItemCheckedChange: (GroceryItem, Boolean) -> Unit,
    private val onItemEdit: (GroceryItem) -> Unit
) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    private var groceryList = listOf<GroceryItem>()

    fun submitList(list: List<GroceryItem>) {
        groceryList = list
        notifyDataSetChanged()
    }

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkGrocery)
        val label = itemView.findViewById<TextView>(R.id.textGroceryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grocery, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val item = groceryList[position]
        // Format text with amount, unit, and name
        val unitText = if (item.unit.isNotEmpty()) "${item.unit} " else ""
        holder.label.text = "${item.amount} $unitText${item.name}"
        
        // Set checkbox state
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isChecked
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onItemCheckedChange(item, isChecked)
        }
        
        // Set item click listener for editing
        holder.itemView.setOnClickListener {
            onItemEdit(item)
        }
        
        // Apply strikethrough if item is checked
        if (item.isChecked) {
            holder.label.paintFlags = holder.label.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.label.paintFlags = holder.label.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = groceryList.size
}

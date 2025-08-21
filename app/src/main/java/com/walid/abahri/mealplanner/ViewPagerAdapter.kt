package com.walid.abahri.mealplanner

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.walid.abahri.mealplanner.UI.GroceryListFragment
import com.walid.abahri.mealplanner.UI.MealPlanFragment
import com.walid.abahri.mealplanner.UI.ProfileFragment
import com.walid.abahri.mealplanner.UI.RecipeListFragment

/**
 * Adapter for the ViewPager2 to handle swiping between main app screens
 */
class ViewPagerAdapter(activity: FragmentActivity, private val forceRefresh: Boolean = false) : FragmentStateAdapter(activity) {

    // Define the fragments to show in the ViewPager
    private val fragments = listOf(
        MealPlanFragment(),
        RecipeListFragment(),
        GroceryListFragment(),
        ProfileFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

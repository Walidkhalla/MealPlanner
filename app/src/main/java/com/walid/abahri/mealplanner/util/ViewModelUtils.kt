package com.walid.abahri.mealplanner.util

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModel
import com.walid.abahri.mealplanner.ViewModel.RecipeViewModelFactory

/**
 * Helper functions for ViewModel creation to ensure consistency
 * throughout the app and avoid the "Cannot create an instance of RecipeViewModel" error
 */
object ViewModelUtils {
    
    /**
     * Creates a RecipeViewModel with the proper factory for a Fragment
     */
    fun getRecipeViewModel(fragment: Fragment): RecipeViewModel {
        val application = fragment.requireActivity().application
        val factory = RecipeViewModelFactory(application)
        return ViewModelProvider(fragment, factory)[RecipeViewModel::class.java]
    }
    
    /**
     * Creates a RecipeViewModel with the proper factory for an Activity
     */
    fun getRecipeViewModel(activity: FragmentActivity): RecipeViewModel {
        val application = activity.application
        val factory = RecipeViewModelFactory(application)
        return ViewModelProvider(activity, factory)[RecipeViewModel::class.java]
    }
    
    /**
     * Creates a RecipeViewModel with a shared scope (activity-level) from a fragment
     * This ensures the same ViewModel instance is used across multiple fragments
     */
    fun getSharedRecipeViewModel(fragment: Fragment): RecipeViewModel {
        val application = fragment.requireActivity().application
        val factory = RecipeViewModelFactory(application)
        return ViewModelProvider(fragment.requireActivity(), factory)[RecipeViewModel::class.java]
    }
}

package com.walid.abahri.mealplanner

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.walid.abahri.mealplanner.UI.AddEditRecipeFragment
import com.walid.abahri.mealplanner.UI.RecipeDetailFragment
import com.walid.abahri.mealplanner.UI.RecipeSelectionFragment

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottom_navigation)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupSystemInsets()
        
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        
        if (prefs.getBoolean("isLoggedIn", false)) {
            showMainContent()
            setupViewPager()
            setupBottomNavigation()
            setupBackButtonHandling()
            
            val lastActiveFragment = prefs.getString("last_active_fragment", "")
            if (lastActiveFragment == "profileFragment") {
                prefs.edit().remove("last_active_fragment").apply()
                
                viewPager.post {
                    viewPager.setCurrentItem(3, false)
                    bottomNav.selectedItemId = R.id.nav_profile
                }
            }
        } else {
            showAuthFlow()
        }
    }
    
    private fun setupSystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, insets.bottom)
            windowInsets
        }
    }
    
    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        
        viewPager.setPageTransformer(null)
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_meal_plans
                    1 -> bottomNav.selectedItemId = R.id.nav_recipes
                    2 -> bottomNav.selectedItemId = R.id.nav_groceries
                    3 -> bottomNav.selectedItemId = R.id.nav_profile
                }
            }
        })
    }
    
    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_meal_plans -> {
                    viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.nav_recipes -> {
                    viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.nav_groceries -> {
                    viewPager.setCurrentItem(2, true)
                    true
                }
                R.id.nav_profile -> {
                    viewPager.setCurrentItem(3, true)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupBackButtonHandling() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    
                    if (supportFragmentManager.backStackEntryCount == 0) {
                        restoreMainNavigation()
                    }
                } else if (viewPager.currentItem == 0) {
                    finish()
                } else {
                    viewPager.currentItem = viewPager.currentItem - 1
                }
            }
        })
    }
    
    private fun restoreMainNavigation() {
        if (!isFinishing) {
            runOnUiThread {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                if (prefs.getBoolean("isLoggedIn", false)) {
                    showMainContent()
                    
                    findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.VISIBLE
                    findViewById<android.widget.FrameLayout>(R.id.fragment_container).visibility = android.view.View.GONE
                    
                    try {
                        val currentPosition = viewPager.currentItem
                        viewPagerAdapter = ViewPagerAdapter(this, true)
                        viewPager.adapter = viewPagerAdapter
                        viewPager.setCurrentItem(currentPosition, false)
                        viewPager.invalidate()
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error refreshing ViewPager: ${e.message}")
                    }
                } else {
                    showAuthFlow()
                }
            }
        }
    }
    
    fun navigateToRecipeSelection(date: String, mealType: String) {
        val fragment = RecipeSelectionFragment().apply {
            arguments = Bundle().apply {
                putString("date", date)
                putString("mealType", mealType)
            }
        }
        navigateToFragment(fragment)
    }
    
    fun navigateToAddEditRecipe(recipeId: Int = 0) {
        val fragment = AddEditRecipeFragment().apply {
            arguments = Bundle().apply {
                putInt("recipeId", recipeId)
            }
        }
        navigateToFragment(fragment)
    }
    
    fun navigateToRecipeDetail(recipeId: Int) {
        val fragment = RecipeDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("recipeId", recipeId)
            }
        }
        navigateToFragment(fragment)
    }
    
    private fun navigateToFragment(fragment: Fragment) {
        findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.GONE
        findViewById<android.widget.FrameLayout>(R.id.fragment_container).visibility = android.view.View.VISIBLE
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    fun returnToMainScreens() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        
        if (prefs.getBoolean("isLoggedIn", false)) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            
            showMainContent()
            
            findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.VISIBLE
            findViewById<android.widget.FrameLayout>(R.id.fragment_container).visibility = android.view.View.GONE
            
            val currentPosition = viewPager.currentItem
            viewPagerAdapter = ViewPagerAdapter(this, true)
            viewPager.adapter = viewPagerAdapter
            viewPager.setCurrentItem(currentPosition, false)
        } else {
            showAuthFlow()
        }
    }
    
    fun showMainContent() {
        findViewById<androidx.fragment.app.FragmentContainerView>(R.id.nav_host_fragment).visibility = android.view.View.GONE
        findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.VISIBLE
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.VISIBLE
    }
    
    fun showAuthFlow() {
        findViewById<androidx.fragment.app.FragmentContainerView>(R.id.nav_host_fragment).visibility = android.view.View.VISIBLE
        findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.GONE
        findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = android.view.View.GONE
        
        navController.navigate(R.id.loginFragment)
    }
    

}

package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.MainActivity
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.util.TextDrawable
import com.walid.abahri.mealplanner.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment(), DefaultLifecycleObserver {

    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var mealPlansCountText: TextView
    private lateinit var recipesCountText: TextView
    private lateinit var favoritesCountText: TextView
    private lateinit var db: AppDatabase
    private lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().lifecycle.addObserver(this)
        userManager = UserManager.getInstance(requireContext())
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            initializeViews(view)
            setupUserData()
            
            view.post {
                updateStatisticsDisplay()
            }
            
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    updateStatisticsDisplay()
                }
            })
            
        } catch (e: Exception) {
            activity?.let {
                Toast.makeText(it, "Error setting up profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun initializeViews(view: View) {
        try {
            userNameText = view.findViewById(R.id.userName)
            userEmailText = view.findViewById(R.id.userEmail)
            profileImage = view.findViewById(R.id.profileImage)
            
            mealPlansCountText = view.findViewById(R.id.mealPlansCount)
            recipesCountText = view.findViewById(R.id.recipesCount)
            favoritesCountText = view.findViewById(R.id.favoritesCount)
            
            view.findViewById<View>(R.id.logoutButton).setOnClickListener { 
                showLogoutConfirmationDialog() 
            }
            
            view.findViewById<View>(R.id.personalInfoCard).setOnClickListener { showFeatureInDevelopmentDialog("Personal Information") }
            view.findViewById<View>(R.id.notificationsCard).setOnClickListener { showFeatureInDevelopmentDialog("Notifications") }
            view.findViewById<View>(R.id.privacySecurityCard).setOnClickListener { showFeatureInDevelopmentDialog("Privacy & Security") }
            view.findViewById<View>(R.id.sharingPreferencesCard).setOnClickListener { showFeatureInDevelopmentDialog("Sharing Preferences") }
            view.findViewById<View>(R.id.dietaryPreferencesCard).setOnClickListener { showFeatureInDevelopmentDialog("Dietary Preferences") }
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize views: ${e.message}")
        }
    }
    
    private fun setupUserData() {
        try {
            val prefs = context?.getSharedPreferences("app_prefs", 0)
            val username = prefs?.getString("username", "User") ?: "User"
            val fullName = prefs?.getString("userFullName", username) ?: username
            val email = prefs?.getString("userEmail", "user@example.com") ?: "user@example.com"
            
            userNameText.text = fullName
            userEmailText.text = email
            
            val initials = fullName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
            
            val colors = resources.getIntArray(R.array.avatar_colors)
            val colorIndex = username.hashCode().mod(colors.size).let { if (it < 0) -it else it }
            
            val avatarSize = resources.getDimensionPixelSize(R.dimen.avatar_size)
            val drawable = TextDrawable.builder()
                .beginConfig()
                .width(avatarSize)
                .height(avatarSize)
                .endConfig()
                .buildRound(initials, colors[colorIndex])
            
            profileImage.setImageDrawable(drawable)
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateStatisticsDisplay() {
        try {
            val db = AppDatabase.getDatabase(requireContext())
            
            lifecycleScope.launch {
                val userId = userManager.getCurrentUserId()
                val mealPlansCount = withContext(Dispatchers.IO) {
                    try {
                        db.mealPlanDao().getMealPlanCount(userId)
                    } catch (e: Exception) {
                        0
                    }
                }
                
                val recipesCount = withContext(Dispatchers.IO) {
                    try {
                        db.recipeDao().getRecipeCount(userId)
                    } catch (e: Exception) {
                        0
                    }
                }
                
                val favoritesCount = 8
                
                mealPlansCountText.text = mealPlansCount.toString()
                recipesCountText.text = recipesCount.toString()
                favoritesCountText.text = favoritesCount.toString()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Error updating statistics: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    

    
    private fun showFeatureInDevelopmentDialog(featureName: String) {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Coming Soon")
                .setMessage("The $featureName feature is coming soon.")
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    try {
                        context?.getSharedPreferences("app_prefs", 0)?.edit()?.apply {
                            putBoolean("isLoggedIn", false)
                            apply()
                        }
                        
                        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                        
                        (activity as? MainActivity)?.returnToMainScreens()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Error showing logout dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

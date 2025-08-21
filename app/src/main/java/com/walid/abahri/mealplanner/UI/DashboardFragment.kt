package com.walid.abahri.mealplanner.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display welcome message with username
        val prefs = requireActivity().getSharedPreferences("app_prefs", 0)
        val username = prefs.getString("username", null)
        binding.textWelcome.text = if (username != null) "Welcome, $username!" else "Welcome!"

        // Navigate to Recipes List
        binding.buttonRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeListFragment)
        }
        // Navigate to Meal Plan
        binding.buttonMealPlan.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_mealPlanFragment)
        }
        // Navigate to Grocery List
        binding.buttonGrocery.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_groceryListFragment)
        }
        // Logout button: clear SharedPreferences and navigate to Login
        binding.buttonLogout.setOnClickListener {
            prefs.edit().clear().apply()  // remove login info
            // Navigate back to Login. We'll reinitialize nav graph to clear backstack:
            val navController = findNavController()
            navController.popBackStack(R.id.loginFragment, true)
            navController.navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

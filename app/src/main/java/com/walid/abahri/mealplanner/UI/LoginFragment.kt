package com.walid.abahri.mealplanner.UI

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.walid.abahri.mealplanner.MainActivity
import com.walid.abahri.mealplanner.ViewModel.LoginViewModel
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val forcedLogout = arguments?.getBoolean("forced_logout", false) ?: false
        
        if (forcedLogout) {
            prefs.edit().putBoolean("isLoggedIn", false).apply()
            Snackbar.make(binding.root, "You have been logged out.", Snackbar.LENGTH_SHORT).show()
        } else if (prefs.getBoolean("isLoggedIn", false)) {
            try {
                (activity as? MainActivity)?.apply {
                    showMainContent()
                    findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager).setCurrentItem(0, false)
                    findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_meal_plans
                }
                return
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Welcome back! Please log in again.", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        val devButton = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "FORCE LOGOUT (DEV)"
            setBackgroundColor(resources.getColor(R.color.accent, null))
            alpha = 0.7f
            setTextColor(resources.getColor(android.R.color.white, null))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                prefs.edit().putBoolean("isLoggedIn", false).apply()
                Snackbar.make(binding.root, "Forced logout successful", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        (binding.root as? ViewGroup)?.addView(devButton)
        devButton.translationX = 20f
        devButton.translationY = 20f

        setupTextWatchers()
        
        loginViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            binding.buttonLogin.isEnabled = true
            binding.progressBar.visibility = View.GONE
            
            when (result) {
                is LoginViewModel.LoginResult.Success -> {
                    val userManager = com.walid.abahri.mealplanner.util.UserManager.getInstance(requireContext())
                    userManager.storeUserInfo(
                        userId = result.user.id,
                        username = result.user.username,
                        fullName = result.user.fullName,
                        email = result.user.email
                    )
                    
                    prefs.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("username", result.user.username)
                        putString("userFullName", result.user.fullName)
                        putString("userEmail", result.user.email)
                        putString("userProfileImage", result.user.profileImageUrl)
                        apply()
                    }

                    (activity as? MainActivity)?.apply {
                        showMainContent()
                        findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager).setCurrentItem(0, false)
                        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_meal_plans
                    }
                }
                
                is LoginViewModel.LoginResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
                
                is LoginViewModel.LoginResult.NetworkError -> {
                    Snackbar.make(binding.root, "Network error: Please check your connection", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.buttonLogin.setOnClickListener {
            val user = binding.editTextUsername.text.toString().trim()
            val pass = binding.editTextPassword.text.toString().trim()
            
            if (validateInput(user, pass)) {
                binding.buttonLogin.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                
                hideKeyboard()
                loginViewModel.login(user, pass)
            }
        }
        
        binding.textRegister.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            } catch (e: Exception) {
                activity?.let {
                    Snackbar.make(binding.root, "Could not open registration: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupTextWatchers() {
        // Clear errors when user starts typing
        binding.editTextUsername.doOnTextChanged { _, _, _, _ ->
            binding.usernameLayout.error = null
        }
        
        binding.editTextPassword.doOnTextChanged { _, _, _, _ ->
            binding.passwordLayout.error = null
        }
    }
    
    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true
        
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
            isValid = false
        }
        
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        return isValid
    }
    
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.walid.abahri.mealplanner.UI

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.walid.abahri.mealplanner.R
import com.walid.abahri.mealplanner.ViewModel.LoginViewModel
import com.walid.abahri.mealplanner.databinding.FragmentRegisterBinding
import android.util.Patterns

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()
        
        loginViewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            binding.buttonRegister.isEnabled = true
            binding.progressBar.visibility = View.GONE
            
            when (result) {
                is LoginViewModel.RegistrationResult.Success -> {
                    Snackbar.make(binding.root, "Registration successful! Please log in.", Snackbar.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                
                is LoginViewModel.RegistrationResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            val fullName = binding.editTextFullName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            
            if (validateInput(fullName, email, username, password, confirmPassword)) {
                binding.buttonRegister.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                
                hideKeyboard()
                loginViewModel.register(fullName, email, username, password)
            }
        }
        
        binding.textLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
    
    private fun setupTextWatchers() {
        val editTextFields = listOf(
            binding.editTextFullName to binding.fullNameLayout,
            binding.editTextEmail to binding.emailLayout,
            binding.editTextUsername to binding.usernameLayout,
            binding.editTextPassword to binding.passwordLayout,
            binding.editTextConfirmPassword to binding.confirmPasswordLayout
        )
        
        for ((editText, layout) in editTextFields) {
            editText.doOnTextChanged { _, _, _, _ ->
                layout.error = null
            }
        }
    }
    
    private fun validateInput(
        fullName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        
        if (fullName.isEmpty()) {
            binding.fullNameLayout.error = "Full name cannot be empty"
            isValid = false
        }
        
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email address"
            isValid = false
        }
        
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
            isValid = false
        } else if (username.length < 4) {
            binding.usernameLayout.error = "Username must be at least 4 characters"
            isValid = false
        }
        
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        }
        
        return isValid
    }
    
    private fun hideKeyboard() {
        activity?.let { activity ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

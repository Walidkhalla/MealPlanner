package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.AppDatabase
import com.walid.abahri.mealplanner.DB.User
import com.walid.abahri.mealplanner.repository.UserRepository
import com.walid.abahri.mealplanner.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo: UserRepository
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    
    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    // Define sealed class for login results
    sealed class LoginResult {
        data class Success(val user: UserProfile) : LoginResult()
        data class Error(val message: String) : LoginResult()
        object NetworkError : LoginResult()
    }
    
    // Define sealed class for registration results
    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
    }
    
    // User profile data class
    data class UserProfile(
        val id: Int,
        val username: String,
        val fullName: String,
        val email: String,
        val profileImageUrl: String? = null,
        val dietaryPreferences: List<String>? = null
    )

    init {
        // Initialize repository using the database instance
        val userDao = AppDatabase.Companion.getDatabase(application).userDao()
        userRepo = UserRepository(userDao)
        
        // Create sample users for testing
        createSampleUsers()
    }
    
    private fun createSampleUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create admin user
                val adminUser = User(
                    username = "admin", 
                    email = "admin@mealplanner.com", 
                    passwordHash = "admin123", 
                    dailyCalorieGoal = 2000, 
                    dietaryPreferences = "balanced,high-protein"
                )
                userRepo.insertUser(adminUser)
                
                // Create test user
                val testUser = User(
                    username = "test", 
                    email = "test@example.com", 
                    passwordHash = "test123", 
                    dailyCalorieGoal = 1800, 
                    dietaryPreferences = "vegetarian,low-carb"
                )
                userRepo.insertUser(testUser)
            } catch (e: Exception) {
                // Ignore errors during sample creation
            }
        }
    }

    fun login(username: String, password: String) {
        // Show loading state immediately
        viewModelScope.launch {
            try {
                // Simulate network delay for better UX
                kotlinx.coroutines.delay(1000)
                
                // Perform login check on IO thread
                val user = withContext(Dispatchers.IO) {
                    userRepo.getUserByUsername(username)
                }
                
                if (user != null && user.passwordHash == password) {
                    // Store the user ID in UserManager
                    val userManager = UserManager.getInstance(getApplication())
                    userManager.setCurrentUserId(user.id)
                    
                    // Create a UserProfile from the database User
                    val userProfile = UserProfile(
                        id = user.id,
                        username = user.username,
                        fullName = when(user.username) {
                            "admin" -> "Admin User"
                            "test" -> "Test User"
                            else -> "${user.username.replaceFirstChar { it.uppercase() }} User"
                        },
                        email = user.email,
                        profileImageUrl = null,
                        dietaryPreferences = user.dietaryPreferences?.split(",")
                    )
                    
                    // Post success result with user profile
                    _loginResult.postValue(LoginResult.Success(userProfile))
                } else {
                    // Invalid credentials
                    _loginResult.postValue(LoginResult.Error("Invalid username or password"))
                }
            } catch (e: IOException) {
                // Network error
                _loginResult.postValue(LoginResult.NetworkError)
            } catch (e: Exception) {
                // Other errors
                _loginResult.postValue(LoginResult.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }

    fun register(fullName: String, email: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                // Check if username already exists
                val existingUser = withContext(Dispatchers.IO) {
                    userRepo.getUserByUsername(username)
                }
                
                if (existingUser != null) {
                    _registrationResult.postValue(RegistrationResult.Error("Username already exists"))
                    return@launch
                }
                
                // Create new user
                val newUser = User(
                    username = username,
                    email = email,
                    passwordHash = password,  // In a real app, this would be hashed
                    fullName = fullName,
                    dailyCalorieGoal = 2000,  // Default value
                    dietaryPreferences = ""   // Empty by default
                )
                
                // Insert the new user
                withContext(Dispatchers.IO) {
                    userRepo.insertUser(newUser)
                }
                
                // Post success result
                _registrationResult.postValue(RegistrationResult.Success)
                
            } catch (e: Exception) {
                // Other errors (including username exists)
                _registrationResult.postValue(RegistrationResult.Error(e.message ?: "Registration failed"))
            }
        }
    }
}

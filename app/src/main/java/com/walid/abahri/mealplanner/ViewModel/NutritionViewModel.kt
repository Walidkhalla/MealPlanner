package com.walid.abahri.mealplanner.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walid.abahri.mealplanner.DB.*
import com.walid.abahri.mealplanner.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NutritionViewModel(
    application: Application,
    private val nutritionRepository: NutritionRepository
) : AndroidViewModel(application) {

    // Current nutrition goal
    val nutritionGoal = nutritionRepository.getNutritionGoal().asLiveData()

    // Selected date for nutrition tracking
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Daily nutrition progress for selected date
    val dailyNutritionProgress = _selectedDate.flatMapLatest { date ->
        nutritionRepository.getDailyNutritionProgress(date)
    }.asLiveData()

    // Weekly nutrition summary
    private val _weekStartDate = MutableStateFlow(getCurrentWeekStart())
    val weeklyNutritionSummary = _weekStartDate.flatMapLatest { startDate ->
        val endDate = calculateWeekEnd(startDate)
        nutritionRepository.getWeeklyNutritionSummary(startDate, endDate)
    }.asLiveData()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Set or update nutrition goals
     */
    fun setNutritionGoals(
        dailyCalories: Float,
        dailyProtein: Float,
        dailyCarbs: Float,
        dailyFat: Float,
        dailyFiber: Float = 25f,
        dailySugarLimit: Float = 50f,
        dailySodiumLimit: Float = 2300f,
        activityLevel: String = "moderate",
        goalType: String = "maintain"
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            val nutritionGoal = NutritionGoal(
                userId = 0, // Will be set by repository
                dailyCalories = dailyCalories,
                dailyProtein = dailyProtein,
                dailyCarbs = dailyCarbs,
                dailyFat = dailyFat,
                dailyFiber = dailyFiber,
                dailySugarLimit = dailySugarLimit,
                dailySodiumLimit = dailySodiumLimit,
                activityLevel = activityLevel,
                goalType = goalType
            )

            nutritionRepository.insertOrUpdateNutritionGoal(nutritionGoal)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to save nutrition goals: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Change the selected date for nutrition tracking
     */
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    /**
     * Navigate to previous day
     */
    fun goToPreviousDay() {
        val currentDate = _selectedDate.value
        val previousDate = calculatePreviousDate(currentDate)
        _selectedDate.value = previousDate
    }

    /**
     * Navigate to next day
     */
    fun goToNextDay() {
        val currentDate = _selectedDate.value
        val nextDate = calculateNextDate(currentDate)
        _selectedDate.value = nextDate
    }

    /**
     * Set week start date for weekly summary
     */
    fun setWeekStartDate(date: String) {
        _weekStartDate.value = date
    }

    /**
     * Navigate to previous week
     */
    fun goToPreviousWeek() {
        val currentWeekStart = _weekStartDate.value
        val previousWeekStart = calculatePreviousWeek(currentWeekStart)
        _weekStartDate.value = previousWeekStart
    }

    /**
     * Navigate to next week
     */
    fun goToNextWeek() {
        val currentWeekStart = _weekStartDate.value
        val nextWeekStart = calculateNextWeek(currentWeekStart)
        _weekStartDate.value = nextWeekStart
    }

    /**
     * Check if user has nutrition goals set up
     */
    suspend fun hasNutritionGoals(): Boolean {
        return nutritionRepository.hasNutritionGoal()
    }

    /**
     * Delete nutrition goals
     */
    fun deleteNutritionGoals() = viewModelScope.launch {
        try {
            _isLoading.value = true
            nutritionRepository.deleteNutritionGoal()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete nutrition goals: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Helper functions for date calculations
    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun getCurrentWeekStart(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculateWeekEnd(startDate: String): String {
        // Simple calculation - add 6 days to start date
        val parts = startDate.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() + 6
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculatePreviousDate(date: String): String {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() - 1
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculateNextDate(date: String): String {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() + 1
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculatePreviousWeek(date: String): String {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() - 7
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculateNextWeek(date: String): String {
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() + 7
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }
}

/**
 * ViewModelFactory for NutritionViewModel
 */
class NutritionViewModelFactory(
    private val application: Application,
    private val nutritionRepository: NutritionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(application, nutritionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

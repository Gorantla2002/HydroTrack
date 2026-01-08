package uk.ac.tees.mad.s3548263.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3548263.data.model.*
import uk.ac.tees.mad.s3548263.data.repository.UserRepository
import uk.ac.tees.mad.s3548263.utils.Resource

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Loading)
    val user: StateFlow<Resource<User>> = _user

    private val _dailyLog = MutableStateFlow<Resource<DailyLog>>(Resource.Loading)
    val dailyLog: StateFlow<Resource<DailyLog>> = _dailyLog

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    companion object {
        const val MIN_INTAKE_AMOUNT = 1
        const val MAX_WATER_INTAKE = 2000 // ml per single intake
        const val MAX_PROTEIN_INTAKE = 100 // g per single intake
        const val MAX_CALORIE_INTAKE = 1500 // kcal per single intake
        const val MAX_DAILY_WATER = 10000 // ml
        const val MAX_DAILY_PROTEIN = 500 // g
        const val MAX_DAILY_CALORIES = 10000 // kcal
    }

    init {
        loadUserData()
        loadTodayLog()
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserFlow(uid).collect { _user.value = it }
        }
    }

    private fun loadTodayLog() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getTodayLogFlow(uid).collect { _dailyLog.value = it }
        }
    }

    fun addIntake(type: IntakeType, amount: Int): Boolean {
        _validationError.value = null

        val validationResult = validateIntakeAmount(type, amount)
        if (!validationResult.isValid) {
            _validationError.value = validationResult.errorMessage
            return false
        }

        val dailyLimitCheck = checkDailyLimit(type, amount)
        if (!dailyLimitCheck.isValid) {
            _validationError.value = dailyLimitCheck.errorMessage
            return false
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _validationError.value = "User not authenticated"
            return false
        }

        viewModelScope.launch {
            try {
                userRepository.addIntake(uid, type, amount)
                userRepository.updateStreak(uid)

                val userRes = userRepository.getUser(uid)
                val logRes = userRepository.getTodayLog(uid)
                if (userRes is Resource.Success && logRes is Resource.Success) {
                    checkAchievements(uid, userRes.data, logRes.data)
                }
            } catch (e: Exception) {
                _validationError.value = "Failed to add intake: ${e.message}"
            }
        }

        return true
    }


    private fun validateIntakeAmount(type: IntakeType, amount: Int): ValidationResult {
        if (amount < MIN_INTAKE_AMOUNT) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Amount must be at least $MIN_INTAKE_AMOUNT"
            )
        }

        return when (type) {
            IntakeType.WATER -> {
                if (amount > MAX_WATER_INTAKE) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Single water intake cannot exceed $MAX_WATER_INTAKE ml"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
            IntakeType.PROTEIN -> {
                if (amount > MAX_PROTEIN_INTAKE) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Single protein intake cannot exceed $MAX_PROTEIN_INTAKE g"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
            IntakeType.CALORIES -> {
                if (amount > MAX_CALORIE_INTAKE) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Single calorie intake cannot exceed $MAX_CALORIE_INTAKE kcal"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
        }
    }


    private fun checkDailyLimit(type: IntakeType, amount: Int): ValidationResult {
        val currentLog = _dailyLog.value
        if (currentLog !is Resource.Success) {
            return ValidationResult(isValid = true) // Allow if we can't check
        }

        val log = currentLog.data

        return when (type) {
            IntakeType.WATER -> {
                val newTotal = log.totalWater + amount
                if (newTotal > MAX_DAILY_WATER) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Daily water limit ($MAX_DAILY_WATER ml) would be exceeded"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
            IntakeType.PROTEIN -> {
                val newTotal = log.totalProtein + amount
                if (newTotal > MAX_DAILY_PROTEIN) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Daily protein limit ($MAX_DAILY_PROTEIN g) would be exceeded"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
            IntakeType.CALORIES -> {
                val newTotal = log.totalCalories + amount
                if (newTotal > MAX_DAILY_CALORIES) {
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Daily calorie limit ($MAX_DAILY_CALORIES kcal) would be exceeded"
                    )
                } else {
                    ValidationResult(isValid = true)
                }
            }
        }
    }


    private suspend fun checkAchievements(userId: String, user: User, today: DailyLog) {
        AchievementTemplates.allAchievements.forEach { ach ->
            if (!ach.isUnlocked && ach.isUnlocked(user, today)) {
                userRepository.unlockAchievement(userId, ach.achievementId)
            }
        }
    }


    fun clearError() {
        _validationError.value = null
    }
}


data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
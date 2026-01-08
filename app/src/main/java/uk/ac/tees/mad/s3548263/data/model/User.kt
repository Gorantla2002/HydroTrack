package uk.ac.tees.mad.s3548263.data.model

data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val weight: Double = 0.0, // in kg
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val waterGoal: Int = 2000, // ml
    val proteinGoal: Int = 100, // grams
    val calorieGoal: Int = 2000, // kcal
    val unit: MeasurementUnit = MeasurementUnit.METRIC,
    val reminderEnabled: Boolean = true,
    val reminderInterval: Int = 120, // minutes
    val startTime: String = "08:00",
    val endTime: String = "22:00",
    val createdAt: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWaterConsumed: Long = 0, // ml
    val totalProteinConsumed: Long = 0, // grams
    val totalCaloriesConsumed: Long = 0 // kcal
)

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9)
}

enum class MeasurementUnit {
    METRIC, // ml, kg, g
    IMPERIAL // oz, lb
}

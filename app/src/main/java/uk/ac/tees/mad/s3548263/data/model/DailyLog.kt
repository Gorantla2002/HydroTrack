package uk.ac.tees.mad.s3548263.data.model

data class DailyLog(
    val logId: String = "",
    val userId: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val waterEntries: List<IntakeEntry> = emptyList(),
    val proteinEntries: List<IntakeEntry> = emptyList(),
    val calorieEntries: List<IntakeEntry> = emptyList(),
    val totalWater: Int = 0, // ml
    val totalProtein: Int = 0, // grams
    val totalCalories: Int = 0, // kcal
    val waterGoal: Int = 0,
    val proteinGoal: Int = 0,
    val calorieGoal: Int = 0,
    val userStreak: Int = 0, // Add this line
    val timestamp: Long = System.currentTimeMillis()
)

data class IntakeEntry(
    val amount: Int = 0,
    val time: String = "", // Format: HH:mm
    val timestamp: Long = System.currentTimeMillis(),
    val type: IntakeType = IntakeType.WATER,
    val note: String = ""
)

enum class IntakeType {
    WATER,
    PROTEIN,
    CALORIES
}

package uk.ac.tees.mad.s3548263.data.model

data class Achievement(
    val achievementId: String = "",
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val requirement: Int = 0,
    val category: AchievementCategory = AchievementCategory.STREAK,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0
)

enum class AchievementCategory {
    STREAK,
    TOTAL_WATER,
    TOTAL_PROTEIN,
    TOTAL_CALORIES,
    CONSISTENCY
}

object AchievementTemplates {
    val allAchievements = listOf(
        Achievement(
            achievementId = "streak_7",
            title = "Week Warrior",
            description = "Achieve a 7-day hydration streak",
            icon = "Fire",
            requirement = 7,
            category = AchievementCategory.STREAK
        ),
        Achievement(
            achievementId = "streak_30",
            title = "Monthly Master",
            description = "Achieve a 30-day hydration streak",
            icon = "Muscle",
            requirement = 30,
            category = AchievementCategory.STREAK
        ),
        Achievement(
            achievementId = "streak_100",
            title = "Century Champion",
            description = "Achieve a 100-day hydration streak",
            icon = "Crown",
            requirement = 100,
            category = AchievementCategory.STREAK
        ),
        Achievement(
            achievementId = "water_1000l",
            title = "1000L Club",
            description = "Drink 1000 liters of water lifetime",
            icon = "Water Drop",
            requirement = 1_000_000, // ml
            category = AchievementCategory.TOTAL_WATER
        ),
        Achievement(
            achievementId = "protein_10kg",
            title = "Protein Pro",
            description = "Consume 10kg of protein lifetime",
            icon = "Meat",
            requirement = 10_000, // grams
            category = AchievementCategory.TOTAL_PROTEIN
        ),
        Achievement(
            achievementId = "consistency_30",
            title = "Consistency King",
            description = "Hit all goals for 30 days straight",
            icon = "Star",
            requirement = 30,
            category = AchievementCategory.CONSISTENCY
        )
    )
}

fun Achievement.isUnlocked(user: User, todayLog: DailyLog): Boolean {
    return when (this.category) {
        AchievementCategory.STREAK -> user.currentStreak >= requirement
        AchievementCategory.TOTAL_WATER -> user.totalWaterConsumed >= requirement
        AchievementCategory.TOTAL_PROTEIN -> user.totalProteinConsumed >= requirement
        AchievementCategory.TOTAL_CALORIES -> user.totalCaloriesConsumed >= requirement
        AchievementCategory.CONSISTENCY -> {
            false
        }
    }
}
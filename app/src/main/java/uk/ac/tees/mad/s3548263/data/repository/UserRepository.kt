package uk.ac.tees.mad.s3548263.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.s3548263.data.model.*
import uk.ac.tees.mad.s3548263.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

class UserRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    suspend fun getUser(userId: String): Resource<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    fun getUserFlow(userId: String): Flow<Resource<User>> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch user"))
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("User not found"))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user")
        }
    }

    suspend fun getTodayLog(userId: String): Resource<DailyLog> =
        getDailyLog(userId, dateFormat.format(Date()))

    suspend fun getDailyLog(userId: String, date: String): Resource<DailyLog> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(date)
                .get()
                .await()

            val log = document.toObject(DailyLog::class.java)
            if (log != null) {
                Resource.Success(log)
            } else {
                val userRes = getUser(userId)
                val defaultLog = DailyLog(
                    logId = date,
                    userId = userId,
                    date = date,
                    waterGoal = if (userRes is Resource.Success) userRes.data.waterGoal else 2000,
                    proteinGoal = if (userRes is Resource.Success) userRes.data.proteinGoal else 100,
                    calorieGoal = if (userRes is Resource.Success) userRes.data.calorieGoal else 2000
                )
                Resource.Success(defaultLog)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch daily log")
        }
    }

    fun getTodayLogFlow(userId: String): Flow<Resource<DailyLog>> = callbackFlow {
        val today = dateFormat.format(Date())
        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("dailyLogs")
            .document(today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch log"))
                    return@addSnapshotListener
                }

                val log = snapshot?.toObject(DailyLog::class.java)
                if (log != null) {
                    trySend(Resource.Success(log))
                } else {
                    trySend(Resource.Success(
                        DailyLog(
                            logId = today,
                            userId = userId,
                            date = today
                        )
                    ))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addIntake(
        userId: String,
        type: IntakeType,
        amount: Int,
        note: String = ""
    ): Resource<Unit> {
        return try {
            val today = dateFormat.format(Date())
            val currentTime = timeFormat.format(Date())

            val entry = IntakeEntry(
                amount = amount,
                time = currentTime,
                type = type,
                note = note
            )

            val logRef = firestore.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(logRef)
                val log = snapshot.toObject(DailyLog::class.java) ?: run {
                    val user = runBlocking { getUser(userId) }
                    DailyLog(
                        logId = today,
                        userId = userId,
                        date = today,
                        waterGoal = if (user is Resource.Success) user.data.waterGoal else 2000,
                        proteinGoal = if (user is Resource.Success) user.data.proteinGoal else 100,
                        calorieGoal = if (user is Resource.Success) user.data.calorieGoal else 2000
                    )
                }

                val updatedLog = when (type) {
                    IntakeType.WATER -> log.copy(
                        waterEntries = log.waterEntries + entry,
                        totalWater = log.totalWater + amount
                    )
                    IntakeType.PROTEIN -> log.copy(
                        proteinEntries = log.proteinEntries + entry,
                        totalProtein = log.totalProtein + amount
                    )
                    IntakeType.CALORIES -> log.copy(
                        calorieEntries = log.calorieEntries + entry,
                        totalCalories = log.totalCalories + amount
                    )
                }

                transaction.set(logRef, updatedLog)
            }.await()

            updateUserTotals(userId, type, amount)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add intake")
        }
    }

    private suspend fun updateUserTotals(userId: String, type: IntakeType, amount: Int) {
        try {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: return@runTransaction

                val updatedUser = when (type) {
                    IntakeType.WATER -> user.copy(totalWaterConsumed = user.totalWaterConsumed + amount)
                    IntakeType.PROTEIN -> user.copy(totalProteinConsumed = user.totalProteinConsumed + amount)
                    IntakeType.CALORIES -> user.copy(totalCaloriesConsumed = user.totalCaloriesConsumed + amount)
                }

                transaction.set(userRef, updatedUser)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getLogsForDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Resource<List<DailyLog>> {
        return try {
            val documents = firestore.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val logs = documents.mapNotNull { it.toObject(DailyLog::class.java) }
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch logs")
        }
    }

    suspend fun updateStreak(userId: String): Resource<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val today = dateFormat.format(Date())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = dateFormat.format(calendar.time)

            val todayLog = getDailyLog(userId, today)
            val yesterdayLog = getDailyLog(userId, yesterday)

            if (todayLog is Resource.Success && yesterdayLog is Resource.Success) {
                val todayGoalsMet = todayLog.data.totalWater >= todayLog.data.waterGoal &&
                        todayLog.data.totalProtein >= todayLog.data.proteinGoal &&
                        todayLog.data.totalCalories >= todayLog.data.calorieGoal

                val yesterdayGoalsMet = yesterdayLog.data.totalWater >= yesterdayLog.data.waterGoal &&
                        yesterdayLog.data.totalProtein >= yesterdayLog.data.proteinGoal &&
                        yesterdayLog.data.totalCalories >= yesterdayLog.data.calorieGoal

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val user = snapshot.toObject(User::class.java) ?: return@runTransaction

                    val newStreak = if (todayGoalsMet) {
                        if (yesterdayGoalsMet) user.currentStreak + 1 else 1
                    } else {
                        0
                    }

                    val updatedUser = user.copy(
                        currentStreak = newStreak,
                        longestStreak = maxOf(user.longestStreak, newStreak)
                    )

                    transaction.set(userRef, updatedUser)
                }.await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update streak")
        }
    }

    suspend fun unlockAchievement(userId: String, achievementId: String): Resource<Unit> {
        return try {
            val template = AchievementTemplates.allAchievements.find { it.achievementId == achievementId }
                ?: return Resource.Error("Achievement not found")

            val unlockedAchievement = template.copy(
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("achievements")
                .document(achievementId)
                .set(unlockedAchievement)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlock achievement")
        }
    }

    fun getAchievementsFlow(userId: String): Flow<Resource<List<Achievement>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load achievements"))
                    return@addSnapshotListener
                }

                val unlocked = snapshot?.toObjects(Achievement::class.java) ?: emptyList()
                val all = AchievementTemplates.allAchievements.map { template ->
                    unlocked.find { it.achievementId == template.achievementId } ?: template
                }
                trySend(Resource.Success(all))
            }

        awaitClose { listener.remove() }
    }
}
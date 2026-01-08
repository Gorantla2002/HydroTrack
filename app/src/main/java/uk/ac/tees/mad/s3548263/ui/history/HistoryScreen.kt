package uk.ac.tees.mad.s3548263.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3548263.data.model.DailyLog
import uk.ac.tees.mad.s3548263.data.repository.UserRepository
import uk.ac.tees.mad.s3548263.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val repo = UserRepository()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var logs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedMonth) {
        isLoading = true
        val calendar = selectedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val res = repo.getLogsForDateRange(uid, startDate, endDate)
        if (res is Resource.Success) {
            logs = res.data.sortedByDescending { it.date }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthSelector(
                selectedMonth = selectedMonth,
                onMonthChange = { selectedMonth = it }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else if (logs.isEmpty()) {
                EmptyHistoryView()
            } else {
                SummarySection(logs)

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        HistoryCard(log)
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: Calendar,
    onMonthChange: (Calendar) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newMonth = selectedMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, -1)
                    onMonthChange(newMonth)
                }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = dateFormat.format(selectedMonth.time),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            IconButton(
                onClick = {
                    val newMonth = selectedMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, 1)
                    if (!newMonth.after(Calendar.getInstance())) {
                        onMonthChange(newMonth)
                    }
                },
                enabled = run {
                    val nextMonth = selectedMonth.clone() as Calendar
                    nextMonth.add(Calendar.MONTH, 1)
                    !nextMonth.after(Calendar.getInstance())
                }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = if (run {
                            val nextMonth = selectedMonth.clone() as Calendar
                            nextMonth.add(Calendar.MONTH, 1)
                            !nextMonth.after(Calendar.getInstance())
                        }) Color(0xFF2196F3) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SummarySection(logs: List<DailyLog>) {
    val totalDays = logs.size
    val perfectDays = logs.count {
        it.totalWater >= it.waterGoal &&
                it.totalProtein >= it.proteinGoal &&
                it.totalCalories >= it.calorieGoal
    }
    val avgWater = if (logs.isNotEmpty()) logs.sumOf { it.totalWater } / logs.size else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Days",
            value = "$totalDays",
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Perfect",
            value = "$perfectDays",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Avg Water",
            value = "$avgWater ml",
            color = Color(0xFF9C27B0),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistoryCard(log: DailyLog) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    val date = try {
        val parsedDate = dateFormat.parse(log.date)
        displayFormat.format(parsedDate ?: Date())
    } catch (e: Exception) {
        log.date
    }

    val waterProgress = (log.totalWater.toFloat() / log.waterGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val proteinProgress = (log.totalProtein.toFloat() / log.proteinGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val calorieProgress = (log.totalCalories.toFloat() / log.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val goalsAchieved = listOf(
        log.totalWater >= log.waterGoal,
        log.totalProtein >= log.proteinGoal,
        log.totalCalories >= log.calorieGoal
    ).count { it }

    val isPerfectDay = goalsAchieved == 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPerfectDay)
                Color(0xFFF1F8E9)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "$goalsAchieved of 3 goals achieved",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                if (isPerfectDay) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 24.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            IntakeProgressRow(
                emoji = "💧",
                label = "Water",
                current = log.totalWater,
                goal = log.waterGoal,
                unit = "ml",
                progress = waterProgress,
                color = Color(0xFF2196F3)
            )

            Spacer(Modifier.height(12.dp))

            IntakeProgressRow(
                emoji = "💪",
                label = "Protein",
                current = log.totalProtein,
                goal = log.proteinGoal,
                unit = "g",
                progress = proteinProgress,
                color = Color(0xFFE91E63)
            )

            Spacer(Modifier.height(12.dp))

            IntakeProgressRow(
                emoji = "🔥",
                label = "Calories",
                current = log.totalCalories,
                goal = log.calorieGoal,
                unit = "kcal",
                progress = calorieProgress,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun IntakeProgressRow(
    emoji: String,
    label: String,
    current: Int,
    goal: Int,
    unit: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }
            Text(
                "$current / $goal $unit",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "📊",
                fontSize = 64.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No Data Available",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Start tracking to see your history",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
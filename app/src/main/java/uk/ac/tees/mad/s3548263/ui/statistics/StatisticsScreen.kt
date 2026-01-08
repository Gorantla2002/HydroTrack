package uk.ac.tees.mad.s3548263.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    val repo = UserRepository()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var logs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf(Period.WEEK) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedPeriod) {
        isLoading = true
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        when (selectedPeriod) {
            Period.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            Period.MONTH -> calendar.add(Calendar.MONTH, -1)
            Period.THREE_MONTHS -> calendar.add(Calendar.MONTH, -3)
        }

        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val res = repo.getLogsForDateRange(uid, startDate, endDate)
        if (res is Resource.Success) {
            logs = res.data.sortedBy { it.date }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Statistics",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodChange = { selectedPeriod = it }
                    )
                }

                item {
                    OverviewSection(logs)
                }

                item {
                    ChartCard(
                        title = "Water Intake",
                        emoji = "💧",
                        color = Color(0xFF2196F3)
                    ) {
                        if (logs.isEmpty()) {
                            EmptyChartMessage()
                        } else {
                            WaterLineChart(
                                logs = logs,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }

                item {
                    ChartCard(
                        title = "Protein Intake",
                        emoji = "💪",
                        color = Color(0xFFE91E63)
                    ) {
                        if (logs.isEmpty()) {
                            EmptyChartMessage()
                        } else {
                            BarChart(
                                data = logs.map { it.totalProtein.toFloat() },
                                labels = logs.map {
                                    SimpleDateFormat("dd", Locale.getDefault())
                                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) ?: Date())
                                },
                                color = Color(0xFFE91E63),
                                maxValue = logs.maxOfOrNull { it.proteinGoal }?.toFloat() ?: 100f
                            )
                        }
                    }
                }

                item {
                    ChartCard(
                        title = "Calorie Intake",
                        emoji = "🔥",
                        color = Color(0xFFFF9800)
                    ) {
                        if (logs.isEmpty()) {
                            EmptyChartMessage()
                        } else {
                            BarChart(
                                data = logs.map { it.totalCalories.toFloat() },
                                labels = logs.map {
                                    SimpleDateFormat("dd", Locale.getDefault())
                                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) ?: Date())
                                },
                                color = Color(0xFFFF9800),
                                maxValue = logs.maxOfOrNull { it.calorieGoal }?.toFloat() ?: 2000f
                            )
                        }
                    }
                }

                item {
                    StreakCard(logs)
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

enum class Period(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    THREE_MONTHS("3 Months")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: Period,
    onPeriodChange: (Period) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Period.values().forEach { period ->
                val isSelected = period == selectedPeriod
                FilterChip(
                    selected = isSelected,
                    onClick = { onPeriodChange(period) },
                    label = {
                        Text(
                            period.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(logs: List<DailyLog>) {
    val totalDays = logs.size
    val avgWater = if (logs.isNotEmpty()) logs.sumOf { it.totalWater } / logs.size else 0
    val avgProtein = if (logs.isNotEmpty()) logs.sumOf { it.totalProtein } / logs.size else 0
    val avgCalories = if (logs.isNotEmpty()) logs.sumOf { it.totalCalories } / logs.size else 0
    val perfectDays = logs.count {
        it.totalWater >= it.waterGoal &&
                it.totalProtein >= it.proteinGoal &&
                it.totalCalories >= it.calorieGoal
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Days",
                value = "$totalDays",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Perfect Days",
                value = "$perfectDays",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Avg Water",
                value = "$avgWater ml",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Protein",
                value = "$avgProtein g",
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Calories",
                value = "$avgCalories",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    emoji: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(Modifier.height(20.dp))

            content()
        }
    }
}

@Composable
private fun WaterLineChart(
    logs: List<DailyLog>,
    color: Color
) {
    if (logs.isEmpty()) {
        EmptyChartMessage()
        return
    }

    val data = logs.map { it.totalWater.toFloat() }
    val maxValue = max(data.maxOrNull() ?: 2000f, logs.maxOfOrNull { it.waterGoal }?.toFloat() ?: 2000f)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val spacing = size.width / (data.size - 1).coerceAtLeast(1)
            val points = data.mapIndexed { index, value ->
                Offset(
                    x = index * spacing,
                    y = size.height - (value / maxValue * size.height)
                )
            }

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = size.height * i / gridLines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, size.height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
            }

            if (points.size > 1) {
                drawPoints(
                    points = points,
                    pointMode = PointMode.Polygon,
                    color = color,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }

            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = color,
                    radius = 6f,
                    center = point
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayDates = if (logs.size <= 7) logs else logs.filterIndexed { index, _ ->
                index == 0 || index == logs.size / 2 || index == logs.size - 1
            }

            displayDates.forEach { log ->
                Text(
                    text = SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(log.date) ?: Date()),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun BarChart(
    data: List<Float>,
    labels: List<String>,
    color: Color,
    maxValue: Float
) {
    if (data.isEmpty()) {
        EmptyChartMessage()
        return
    }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val barWidth = (size.width / data.size) * 0.7f
            val spacing = size.width / data.size

            data.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * size.height
                val x = index * spacing + (spacing - barWidth) / 2

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )

                drawRect(
                    color = color,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    style = Stroke(width = 2f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val displayLabels = if (labels.size <= 7) labels else labels.filterIndexed { index, _ ->
                index == 0 || index == labels.size / 2 || index == labels.size - 1
            }

            displayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StreakCard(logs: List<DailyLog>) {
    val currentStreak = calculateCurrentStreak(logs)
    val longestStreak = calculateLongestStreak(logs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔥", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Streak Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakInfo(
                    label = "Current Streak",
                    value = "$currentStreak",
                    color = Color(0xFFFF9800)
                )
                StreakInfo(
                    label = "Best Streak",
                    value = "$longestStreak",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StreakInfo(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyChartMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No data available",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

private fun calculateCurrentStreak(logs: List<DailyLog>): Int {
    if (logs.isEmpty()) return 0

    val sortedLogs = logs.sortedByDescending { it.date }
    var streak = 0
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    for (log in sortedLogs) {
        if (log.totalWater >= log.waterGoal &&
            log.totalProtein >= log.proteinGoal &&
            log.totalCalories >= log.calorieGoal) {
            streak++
        } else {
            break
        }
    }

    return streak
}

private fun calculateLongestStreak(logs: List<DailyLog>): Int {
    if (logs.isEmpty()) return 0

    val sortedLogs = logs.sortedBy { it.date }
    var longestStreak = 0
    var currentStreak = 0

    for (log in sortedLogs) {
        if (log.totalWater >= log.waterGoal &&
            log.totalProtein >= log.proteinGoal &&
            log.totalCalories >= log.calorieGoal) {
            currentStreak++
            longestStreak = max(longestStreak, currentStreak)
        } else {
            currentStreak = 0
        }
    }

    return longestStreak
}
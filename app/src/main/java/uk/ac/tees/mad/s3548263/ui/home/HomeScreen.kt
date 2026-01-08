package uk.ac.tees.mad.s3548263.ui.home

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import uk.ac.tees.mad.s3548263.data.model.IntakeType
import uk.ac.tees.mad.s3548263.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val dailyLog by viewModel.dailyLog.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf<IntakeType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(validationError) {
        validationError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFEF5350),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HydroTrack Plus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = { showMenu = false; onNavigateToProfile() },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = { showMenu = false; onNavigateToHistory() },
                            leadingIcon = { Icon(Icons.Default.History, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Statistics") },
                            onClick = { showMenu = false; onNavigateToStatistics() },
                            leadingIcon = { Icon(Icons.Default.BarChart, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Achievements") },
                            onClick = { showMenu = false; onNavigateToAchievements() },
                            leadingIcon = { Icon(Icons.Default.EmojiEvents, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { showMenu = false; onNavigateToSettings() },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                            onClick = { onLogout() },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomDialog = IntakeType.WATER },
                containerColor = Color(0xFF2196F3),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add Water",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    WelcomeCard(user = user)
                }
            }

            item {
                Text(
                    "Today's Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 100))
                ) {
                    IntakeSection(
                        title = "Water Intake",
                        emoji = "💧",
                        color = Color(0xFF2196F3),
                        current = dailyLog.getCurrent(IntakeType.WATER),
                        goal = dailyLog.getGoal(IntakeType.WATER),
                        unit = "ml",
                        onAdd = { amount ->
                            if (!viewModel.addIntake(IntakeType.WATER, amount)) {
                                // Error handled via snackbar from validationError state
                            }
                        },
                        amounts = listOf(250, 500, 1000),
                        onCustom = { showCustomDialog = IntakeType.WATER }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 200))
                ) {
                    IntakeSection(
                        title = "Protein Intake",
                        emoji = "💪",
                        color = Color(0xFFE91E63),
                        current = dailyLog.getCurrent(IntakeType.PROTEIN),
                        goal = dailyLog.getGoal(IntakeType.PROTEIN),
                        unit = "g",
                        onAdd = { amount ->
                            if (!viewModel.addIntake(IntakeType.PROTEIN, amount)) {
                                // Error handled via snackbar
                            }
                        },
                        amounts = listOf(10, 25, 50),
                        onCustom = { showCustomDialog = IntakeType.PROTEIN }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 300))
                ) {
                    IntakeSection(
                        title = "Calorie Intake",
                        emoji = "🔥",
                        color = Color(0xFFFF9800),
                        current = dailyLog.getCurrent(IntakeType.CALORIES),
                        goal = dailyLog.getGoal(IntakeType.CALORIES),
                        unit = "kcal",
                        onAdd = { amount ->
                            if (!viewModel.addIntake(IntakeType.CALORIES, amount)) {
                                // Error handled via snackbar
                            }
                        },
                        amounts = listOf(100, 250, 500),
                        onCustom = { showCustomDialog = IntakeType.CALORIES }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 400))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAction(
                            label = "History",
                            icon = Icons.Default.History,
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            label = "Statistics",
                            icon = Icons.Default.BarChart,
                            onClick = onNavigateToStatistics,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAction(
                            label = "Achievements",
                            icon = Icons.Default.EmojiEvents,
                            onClick = onNavigateToAchievements,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp)) // Space for FAB
            }
        }

        showCustomDialog?.let { type ->
            CustomAmountDialog(
                type = type,
                onDismiss = { showCustomDialog = null },
                onConfirm = { amount ->
                    if (viewModel.addIntake(type, amount)) {
                        showCustomDialog = null
                    }
                }
            )
        }
    }
}

@Composable
private fun WelcomeCard(user: Resource<uk.ac.tees.mad.s3548263.data.model.User>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF2196F3).copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF1976D2)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.getInitial(),
                        color = Color(0xFF2196F3),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Welcome back!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user.getName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun IntakeSection(
    title: String,
    emoji: String,
    color: Color,
    current: Int,
    goal: Int,
    unit: String,
    onAdd: (Int) -> Unit,
    amounts: List<Int>,
    onCustom: () -> Unit
) {
    val progress = (current.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = color.copy(alpha = 0.15f)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 28.sp
                        )
                    }
                    Column {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "$current / $goal $unit",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                amounts.forEach { amount ->
                    Button(
                        onClick = { onAdd(amount) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color.copy(alpha = 0.12f),
                            contentColor = color
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            "+$amount",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCustom,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = color
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(color, color))
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Custom",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF2196F3).copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Color(0xFF2196F3)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun Resource<uk.ac.tees.mad.s3548263.data.model.User>.getName() =
    (this as? Resource.Success)?.data?.displayName ?: "Hydration Hero"

private fun Resource<uk.ac.tees.mad.s3548263.data.model.User>.getInitial() =
    getName().firstOrNull()?.uppercaseChar()?.toString() ?: "H"

private fun Resource<uk.ac.tees.mad.s3548263.data.model.DailyLog>.getCurrent(type: IntakeType) =
    (this as? Resource.Success)?.data?.let {
        when (type) {
            IntakeType.WATER -> it.totalWater
            IntakeType.PROTEIN -> it.totalProtein
            IntakeType.CALORIES -> it.totalCalories
        }
    } ?: 0

private fun Resource<uk.ac.tees.mad.s3548263.data.model.DailyLog>.getGoal(type: IntakeType) =
    (this as? Resource.Success)?.data?.let {
        when (type) {
            IntakeType.WATER -> it.waterGoal
            IntakeType.PROTEIN -> it.proteinGoal
            IntakeType.CALORIES -> it.calorieGoal
        }
    } ?: 2000
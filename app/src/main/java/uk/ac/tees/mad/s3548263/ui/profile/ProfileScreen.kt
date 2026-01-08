package uk.ac.tees.mad.s3548263.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3548263.data.model.ActivityLevel
import uk.ac.tees.mad.s3548263.data.model.User
import uk.ac.tees.mad.s3548263.data.repository.UserRepository
import uk.ac.tees.mad.s3548263.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val userRepo = UserRepository()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var displayName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var showActivityDialog by remember { mutableStateOf(false) }

    var waterGoal by remember { mutableStateOf("") }
    var proteinGoal by remember { mutableStateOf("") }
    var calorieGoal by remember { mutableStateOf("") }

    var autoCalculate by remember { mutableStateOf(true) }

    var weightError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var waterGoalError by remember { mutableStateOf<String?>(null) }
    var proteinGoalError by remember { mutableStateOf<String?>(null) }
    var calorieGoalError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val MIN_WEIGHT = 30.0
    val MAX_WEIGHT = 300.0
    val MIN_NAME_LENGTH = 2
    val MAX_NAME_LENGTH = 50
    val MIN_WATER = 500
    val MAX_WATER = 5000
    val MIN_PROTEIN = 20
    val MAX_PROTEIN = 300
    val MIN_CALORIES = 1000
    val MAX_CALORIES = 5000

    LaunchedEffect(Unit) {
        val res = userRepo.getUser(uid)
        if (res is Resource.Success) {
            user = res.data
            displayName = res.data.displayName
            weight = if (res.data.weight > 0) res.data.weight.toString() else ""
            activityLevel = res.data.activityLevel
            waterGoal = res.data.waterGoal.toString()
            proteinGoal = res.data.proteinGoal.toString()
            calorieGoal = res.data.calorieGoal.toString()
        }
        isLoading = false
    }

    LaunchedEffect(weight, activityLevel) {
        if (autoCalculate && weight.isNotEmpty()) {
            val w = weight.toDoubleOrNull() ?: 0.0
            if (w >= MIN_WEIGHT && w <= MAX_WEIGHT) {
                val calculatedWater = (w * 35).toInt()
                waterGoal = calculatedWater.toString()

                val calculatedProtein = (w * 1.6).toInt()
                proteinGoal = calculatedProtein.toString()


                val bmr = 10 * w + 6.25 * 170 - 5 * 30 + 5
                val calculatedCalories = (bmr * activityLevel.multiplier).toInt()
                calorieGoal = calculatedCalories.toString()

                waterGoalError = null
                proteinGoalError = null
                calorieGoalError = null
            }
        }
    }


    fun validateInputs(): Boolean {
        var isValid = true

        if (displayName.trim().length < MIN_NAME_LENGTH) {
            nameError = "Name must be at least $MIN_NAME_LENGTH characters"
            isValid = false
        } else if (displayName.trim().length > MAX_NAME_LENGTH) {
            nameError = "Name must be less than $MAX_NAME_LENGTH characters"
            isValid = false
        } else {
            nameError = null
        }

        val w = weight.toDoubleOrNull()
        if (w == null) {
            weightError = "Please enter a valid weight"
            isValid = false
        } else if (w < MIN_WEIGHT) {
            weightError = "Weight must be at least $MIN_WEIGHT kg"
            isValid = false
        } else if (w > MAX_WEIGHT) {
            weightError = "Weight must be less than $MAX_WEIGHT kg"
            isValid = false
        } else {
            weightError = null
        }

        val water = waterGoal.toIntOrNull()
        if (water == null || water <= 0) {
            waterGoalError = "Please enter a valid water goal"
            isValid = false
        } else if (water < MIN_WATER) {
            waterGoalError = "Recommended minimum: $MIN_WATER ml"
        } else if (water > MAX_WATER) {
            waterGoalError = "This seems unusually high (max: $MAX_WATER ml)"
        } else {
            waterGoalError = null
        }

        val protein = proteinGoal.toIntOrNull()
        if (protein == null || protein <= 0) {
            proteinGoalError = "Please enter a valid protein goal"
            isValid = false
        } else if (protein < MIN_PROTEIN) {
            proteinGoalError = "Recommended minimum: $MIN_PROTEIN g"
        } else if (protein > MAX_PROTEIN) {
            proteinGoalError = "This seems unusually high (max: $MAX_PROTEIN g)"
        } else {
            proteinGoalError = null
        }

        val calories = calorieGoal.toIntOrNull()
        if (calories == null || calories <= 0) {
            calorieGoalError = "Please enter a valid calorie goal"
            isValid = false
        } else if (calories < MIN_CALORIES) {
            calorieGoalError = "Recommended minimum: $MIN_CALORIES kcal"
        } else if (calories > MAX_CALORIES) {
            calorieGoalError = "This seems unusually high (max: $MAX_CALORIES kcal)"
        } else {
            calorieGoalError = null
        }

        return isValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
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
        containerColor = Color(0xFFF8F9FA),
        snackbarHost = {
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text("Profile updated successfully!", color = Color.White)
                }
            }
        }
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
            user?.let { u ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                        color = Color(0xFF2196F3),
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    u.email,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    SectionCard(title = "Basic Information") {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                if (it.length <= MAX_NAME_LENGTH) {
                                    displayName = it
                                    nameError = null
                                }
                            },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,1}$"))) {
                                    weight = input
                                    weightError = null
                                }
                            },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = weightError != null,
                            supportingText = weightError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showActivityDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DirectionsRun,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3)
                                    )
                                    Column {
                                        Text(
                                            "Activity Level",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            activityLevel.name.replace("_", " "),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionCard(title = "Daily Goals") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-calculate based on weight",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Uses scientifically recommended ratios",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = autoCalculate,
                                onCheckedChange = { autoCalculate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2196F3)
                                )
                            )
                        }

                        if (!autoCalculate) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "⚠️ Manual mode: Set your own goals",
                                fontSize = 12.sp,
                                color = Color(0xFFFF9800)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        GoalField(
                            label = "Water Goal (ml)",
                            value = waterGoal,
                            onChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    waterGoal = it
                                    waterGoalError = null
                                }
                            },
                            icon = "💧",
                            enabled = !autoCalculate,
                            error = waterGoalError
                        )

                        Spacer(Modifier.height(12.dp))

                        GoalField(
                            label = "Protein Goal (g)",
                            value = proteinGoal,
                            onChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    proteinGoal = it
                                    proteinGoalError = null
                                }
                            },
                            icon = "💪",
                            enabled = !autoCalculate,
                            error = proteinGoalError
                        )

                        Spacer(Modifier.height(12.dp))

                        GoalField(
                            label = "Calorie Goal (kcal)",
                            value = calorieGoal,
                            onChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    calorieGoal = it
                                    calorieGoalError = null
                                }
                            },
                            icon = "🔥",
                            enabled = !autoCalculate,
                            error = calorieGoalError
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (validateInputs()) {
                                scope.launch {
                                    isSaving = true
                                    val updated = u.copy(
                                        displayName = displayName.trim(),
                                        weight = weight.toDoubleOrNull() ?: u.weight,
                                        activityLevel = activityLevel,
                                        waterGoal = waterGoal.toIntOrNull() ?: u.waterGoal,
                                        proteinGoal = proteinGoal.toIntOrNull() ?: u.proteinGoal,
                                        calorieGoal = calorieGoal.toIntOrNull() ?: u.calorieGoal
                                    )
                                    val result = userRepo.updateUser(updated)
                                    isSaving = false

                                    if (result is Resource.Success) {
                                        showSuccessMessage = true
                                        kotlinx.coroutines.delay(2000)
                                        showSuccessMessage = false
                                        onBack()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Save Changes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }

                if (showActivityDialog) {
                    ActivityLevelDialog(
                        currentLevel = activityLevel,
                        onDismiss = { showActivityDialog = false },
                        onSelect = {
                            activityLevel = it
                            showActivityDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

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
                content()
            }
        }
    }
}

@Composable
private fun GoalField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    icon: String,
    enabled: Boolean = true,
    error: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) Color(0xFF2196F3).copy(alpha = 0.12f)
                    else Color.Gray.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                isError = error != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
                    disabledLabelColor = Color.Gray
                )
            )
            if (error != null) {
                Text(
                    error,
                    fontSize = 11.sp,
                    color = if (error.contains("Recommended")) Color(0xFFFF9800) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityLevelDialog(
    currentLevel: ActivityLevel,
    onDismiss: () -> Unit,
    onSelect: (ActivityLevel) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Activity Level",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityLevel.values().forEach { level ->
                    val description = when (level) {
                        ActivityLevel.SEDENTARY -> "Little to no exercise"
                        ActivityLevel.LIGHT -> "Exercise 1-3 days/week"
                        ActivityLevel.MODERATE -> "Exercise 3-5 days/week"
                        ActivityLevel.ACTIVE -> "Exercise 6-7 days/week"
                        ActivityLevel.VERY_ACTIVE -> "Very intense exercise daily"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(level) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (level == currentLevel)
                                Color(0xFF2196F3).copy(alpha = 0.15f)
                            else
                                Color.White
                        ),
                        border = if (level == currentLevel)
                            ButtonDefaults.outlinedButtonBorder.copy(
                                width = 2.dp,
                                brush = Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF2196F3)))
                            )
                        else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    level.name.replace("_", " "),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                if (level == currentLevel) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                description,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
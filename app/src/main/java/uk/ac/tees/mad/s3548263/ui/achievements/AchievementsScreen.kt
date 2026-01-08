package uk.ac.tees.mad.s3548263.ui.achievements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.s3548263.data.model.Achievement
import uk.ac.tees.mad.s3548263.data.repository.UserRepository
import uk.ac.tees.mad.s3548263.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val userRepo = UserRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userRepo.getAchievementsFlow(userId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    achievements = result.data
                    isLoading = false
                }
                is Resource.Error -> isLoading = false
                is Resource.Loading -> isLoading = true
            }
        }
    }

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Achievements", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                ProgressHeader(unlockedCount, totalCount, progress)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementCard(achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(unlockedCount: Int, totalCount: Int, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300))
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Trophy", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("Achievement Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("$unlockedCount of $totalCount unlocked", fontSize = 14.sp, color = Color.White.copy(0.9f))

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}% Complete", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val scale by animateFloatAsState(
        targetValue = if (achievement.isUnlocked) 1f else 0.95f,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(if (achievement.isUnlocked) 4.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .alpha(if (achievement.isUnlocked) 1f else 0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (achievement.isUnlocked)
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFD700).copy(0.3f), Color(0xFFFFD700).copy(0.1f))
                                )
                            else
                                Brush.radialGradient(
                                    colors = listOf(Color.Gray.copy(0.2f), Color.Gray.copy(0.1f))
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (achievement.isUnlocked) {
                        Text(achievement.icon, fontSize = 48.sp)
                    } else {
                        Icon(Icons.Default.Lock, "Locked", tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = achievement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (achievement.isUnlocked) Color(0xFF1A1A1A) else Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))

                if (achievement.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CAF50).copy(0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Unlocked", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    }
                    if (achievement.unlockedAt > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(achievement.unlockedAt)),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Locked", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                    }
                }
            }

            if (achievement.isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(200f, 200f)
                            )
                        )
                )
            }
        }
    }
}
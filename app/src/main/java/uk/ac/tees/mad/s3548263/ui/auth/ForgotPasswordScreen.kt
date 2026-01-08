package uk.ac.tees.mad.s3548263.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3548263.data.repository.AuthRepository
import uk.ac.tees.mad.s3548263.utils.Resource

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _resetState = MutableStateFlow<Resource<Unit>?>(null)
    val resetState: StateFlow<Resource<Unit>?> = _resetState

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetState.value = Resource.Loading
            _resetState.value = authRepository.resetPassword(email)
        }
    }

    fun resetState() {
        _resetState.value = Resource.Loading
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val resetState by viewModel.resetState.collectAsState()

    LaunchedEffect(resetState) {
        when (resetState) {
            is Resource.Success -> {
                message = "Password reset email sent! Check your inbox."
                showSuccess = true
                viewModel.resetState()
            }
            is Resource.Error -> {
                message = (resetState as Resource.Error).message
                showError = true
            }
            Resource.Loading -> { /* maybe show spinner */ }
            null -> { /* initial state, do nothing */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF00BCD4))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "🔐", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = "Forgot Password?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Enter your email to receive a password reset link",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (email.isNotBlank()) {
                                viewModel.resetPassword(email)
                            } else {
                                message = "Please enter your email"
                                showError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = resetState !is Resource.Loading
                    ) {
                        if (resetState is Resource.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Send Reset Link", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (showSuccess) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = Color(0xFF4CAF50),
                action = { TextButton(onClick = { showSuccess = false; onNavigateBack() }) { Text("OK", color = Color.White) } }
            ) {
                Text(message, color = Color.White)
            }
        }

        if (showError) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { showError = false }) { Text("Dismiss") } }
            ) {
                Text(message)
            }
        }
    }
}

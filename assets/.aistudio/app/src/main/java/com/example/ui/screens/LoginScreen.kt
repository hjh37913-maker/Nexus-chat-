package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NexusViewModel
import com.example.ui.theme.NexusBackground
import com.example.ui.theme.NexusPrimary
import com.example.ui.theme.NexusSurface
import com.example.ui.theme.NexusAccentCyan
import com.example.ui.theme.NexusTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: NexusViewModel,
    onNavigateToDashboard: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val localUser by viewModel.localUser.collectAsState()

    // If already logged in, automatically navigate away
    LaunchedEffect(localUser) {
        if (localUser != null) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("login_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NexusBackground,
                            Color(0xFF070F14)
                        )
                    )
                )
                .drawBehind {
                    // Draw futuristic subtle lines/nodes
                    drawCircle(
                        color = NexusPrimary.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.4f,
                        center = Offset(size.width * 0.1f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = NexusAccentCyan.copy(alpha = 0.05f),
                        radius = size.minDimension * 0.3f,
                        center = Offset(size.width * 0.9f, size.height * 0.8f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .widthIn(max = 450.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Futuristic Glowing NEXUS Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NexusPrimary, NexusAccentCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NX",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-2).sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "NEXUS",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Будущее безопасного общения",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NexusAccentCyan,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Card with fields
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexusPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = NexusSurface.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Создать аккаунт",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Nickname Field
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                if (it.isNotEmpty()) errorMessage = ""
                            },
                            label = { Text("Имя или Никнейм") },
                            placeholder = { Text("Например: Александр") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NexusPrimary
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = NexusAccentCyan,
                                unfocusedBorderColor = Color(0xFF2C3B4E)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("display_name_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { input ->
                                // Strip illegal spaces/specials and keep letters/numbers/underscore
                                val filtered = input.filter { it.isLetterOrDigit() || it == '_' }
                                username = filtered
                                if (filtered.isNotEmpty()) errorMessage = ""
                            },
                            label = { Text("Юзернейм (@username)") },
                            placeholder = { Text("alex_nexus") },
                            prefix = { Text("@", color = NexusAccentCyan) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = NexusAccentCyan
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = NexusAccentCyan,
                                unfocusedBorderColor = Color(0xFF2C3B4E)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        // Error State Display
                        AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                when {
                                    displayName.trim().isEmpty() -> {
                                        errorMessage = "Пожалуйста, введите ваше имя."
                                    }
                                    username.trim().isEmpty() -> {
                                        errorMessage = "Пожалуйста, введите желаемый юзернейм."
                                    }
                                    username.trim().length < 3 -> {
                                        errorMessage = "Юзернейм должен быть не короче 3 символов."
                                    }
                                    else -> {
                                        val cleanUsername = "@${username.trim().lowercase()}"
                                        viewModel.registerUser(cleanUsername, displayName.trim())
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Войти в сеть NEXUS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Запуская приложение, вы соглашаетесь на хранение переписки локально в защищенном хранилище SQLite.",
                    fontSize = 11.sp,
                    color = NexusTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

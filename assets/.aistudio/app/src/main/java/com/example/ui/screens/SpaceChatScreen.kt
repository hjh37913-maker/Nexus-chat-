package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpaceMessageEntity
import com.example.ui.NexusViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceChatScreen(
    viewModel: NexusViewModel,
    spaceId: String,
    onNavigateBack: () -> Unit
) {
    val localUser by viewModel.localUser.collectAsState()
    val spaces by viewModel.spaces.collectAsState()
    val messages by viewModel.currentSpaceMessages.collectAsState()

    val space = remember(spaces, spaceId) {
        spaces.find { it.spaceId == spaceId }
    }

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val isEn = localUser?.appLanguage == "EN"
    fun t(ru: String, en: String) = if (isEn) en else ru

    // Set active select space
    LaunchedEffect(spaceId) {
        viewModel.selectSpace(spaceId)
    }

    // Scroll to bottom when space message history grows
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (localUser == null || space == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NexusAccentCyan)
        }
        return
    }

    // Dynamic limits based on subscription
    val limit = when (localUser!!.subscriptionType) {
        "Free" -> 50
        "Nexus+" -> 200
        else -> 1000 // Nexus Max
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = space.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${space.memberCount} " + t("участников", "members"),
                            fontSize = 11.sp,
                            color = NexusAccentCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.selectSpace(null)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("Назад", "Back"),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusSurface)
            )
        },
        containerColor = NexusBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Space description card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = NexusSurface.copy(alpha = 0.5f))
            ) {
                Text(
                    text = space.description,
                    color = NexusTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderUsername == localUser!!.username
                    SpaceMessageBubble(
                        message = message,
                        isMe = isMe
                    )
                }
            }

            // Character alert indicators
            val isLimitConflict = textInput.length > limit
            AnimatedVisibility(
                visible = textInput.length >= (limit - 10),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLimitConflict) Color(0xFFD32F2F).copy(alpha = 0.15f)
                            else NexusSurface
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isLimitConflict) {
                            t(
                                "⚠️ Превышен лимит сообщений ($limit симв. для тарифа ${localUser!!.subscriptionType}). Увеличьте тариф в Профиле!",
                                "⚠️ Character limit exceeded ($limit chars for ${localUser!!.subscriptionType} plan). Level up in Profile!"
                            )
                        } else {
                            t(
                                "Осталось символов: ${limit - textInput.length} / $limit",
                                "Chars remaining: ${limit - textInput.length} / $limit"
                            )
                        },
                        color = if (isLimitConflict) Color.Red else NexusAccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Send fields Row
            Surface(
                color = NexusSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = null,
                        tint = NexusTextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text(t("Написать в Пространство...", "Broadcast to space..."), fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = false,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp)
                            .testTag("space_message_input_box")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val isMessageValid = textInput.isNotBlank() && textInput.length <= limit
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isMessageValid) NexusPrimary else Color(0xFF233040))
                            .clickable(enabled = isMessageValid) {
                                viewModel.sendSpaceMessage(textInput.trim())
                                textInput = ""
                                focusManager.clearFocus()
                            }
                            .testTag("send_space_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = t("Отправить", "Send"),
                            tint = if (isMessageValid) Color.White else NexusTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceMessageBubble(
    message: SpaceMessageEntity,
    isMe: Boolean
) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            // Group Sender Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(message.senderAvatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (message.senderDisplayName.firstOrNull() ?: '@').toString().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) NexusBubbleMe else NexusBubbleOther
            ),
            modifier = Modifier.widthIn(min = 80.dp, max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isMe) {
                    Text(
                        text = message.senderDisplayName,
                        color = Color(message.senderAvatarColor).copy(alpha = 1f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sdf.format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = NexusTextSecondary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

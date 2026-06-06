package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatPartnerEntity
import com.example.data.LocalUserEntity
import com.example.data.SpaceEntity
import com.example.ui.NexusViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: NexusViewModel,
    onNavigateToChat: (partnerUsername: String) -> Unit,
    onNavigateToSpace: (spaceId: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val localUser by viewModel.localUser.collectAsState()

    // If local user is cleared, trigger logout navigation
    LaunchedEffect(localUser) {
        if (localUser == null) {
            onLogout()
        }
    }

    if (localUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NexusAccentCyan)
        }
        return
    }

    val user = localUser!!
    val isEn = user.appLanguage == "EN"

    // Translation helper
    fun t(ru: String, en: String): String = if (isEn) en else ru

    Scaffold(
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = NexusSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = if (selectedTab == 0) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubble, contentDescription = null) },
                    label = { Text(t("Чаты", "Chats"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NexusAccentCyan,
                        selectedTextColor = NexusAccentCyan,
                        indicatorColor = NexusSurface.copy(alpha = 0.5f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_chats_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = if (selectedTab == 1) Icons.Filled.Groups else Icons.Outlined.Groups, contentDescription = null) },
                    label = { Text(t("Пространства", "Spaces"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NexusAccentCyan,
                        selectedTextColor = NexusAccentCyan,
                        indicatorColor = NexusSurface.copy(alpha = 0.5f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_spaces_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    label = { Text(t("Поиск", "Search"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NexusAccentCyan,
                        selectedTextColor = NexusAccentCyan,
                        indicatorColor = NexusSurface.copy(alpha = 0.5f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_search_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(imageVector = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = null) },
                    label = { Text(t("Профиль", "Profile"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NexusAccentCyan,
                        selectedTextColor = NexusAccentCyan,
                        indicatorColor = NexusSurface.copy(alpha = 0.5f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        },
        containerColor = NexusBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatsTab(viewModel, onNavigateToChat, t("Список чатов пуст", "No active chats yet"))
                1 -> SpacesTab(viewModel, onNavigateToSpace, user)
                2 -> SearchTab(viewModel, onNavigateToChat, user)
                3 -> ProfileTab(viewModel, user)
            }
        }
    }
}

// -------------------------------------------------------------
// CHATS TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    viewModel: NexusViewModel,
    onNavigateToChat: (String) -> Unit,
    emptyText: String
) {
    val activeChats by viewModel.activeChats.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NEXUS MESSENGER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = NexusAccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (activeChats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = NexusTextSecondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = emptyText, color = NexusTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(activeChats) { chat ->
                    ChatListItem(chat = chat, onClick = { onNavigateToChat(chat.partner.username) })
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: com.example.ui.ActiveChat,
    onClick: () -> Unit
) {
    val p = chat.partner
    val lastMsg = chat.lastMessage
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(p.avatarColor).copy(alpha = 1f)),
            contentAlignment = Alignment.Center
        ) {
            val letter = p.displayName.firstOrNull()?.toString() ?: "@"
            Text(
                text = letter.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = p.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (p.username == "@durov" || p.username == "@nexus_support") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Partner",
                            tint = NexusVerified,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Text(
                    text = if (lastMsg != null) sdf.format(Date(lastMsg.timestamp)) else "",
                    color = NexusTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lastMsg?.content ?: p.bio,
                    color = if (lastMsg != null) NexusTextSecondary else NexusTextSecondary.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (lastMsg != null) {
                    if (lastMsg.senderUsername != p.username) {
                        // Message sent by me
                        Icon(
                            imageVector = if (lastMsg.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (lastMsg.isRead) NexusAccentCyan else NexusTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (!lastMsg.isRead) {
                        // Unread notification seal
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NexusAccentCyan)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SPACES TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesTab(
    viewModel: NexusViewModel,
    onNavigateToSpace: (String) -> Unit,
    user: LocalUserEntity
) {
    val spaces by viewModel.spaces.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var spaceName by remember { mutableStateOf("") }
    var spaceDescription by remember { mutableStateOf("") }

    val isEn = user.appLanguage == "EN"
    fun t(ru: String, en: String) = if (isEn) en else ru

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NexusPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = NexusBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text(t("Пространства", "Spaces"), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(spaces) { space ->
                    SpaceCardItem(
                        space = space,
                        onJoinToggle = { viewModel.joinOrLeaveSpace(space) },
                        onOpen = { onNavigateToSpace(space.spaceId) },
                        isEn = isEn
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(t("Создать Пространство", "Create Space"), color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = NexusSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = spaceName,
                        onValueChange = { spaceName = it },
                        label = { Text(t("Название пространства", "Space Name")) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = NexusAccentCyan,
                            unfocusedBorderColor = Color(0xFF2C3B4E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("space_name_field")
                    )

                    OutlinedTextField(
                        value = spaceDescription,
                        onValueChange = { spaceDescription = it },
                        label = { Text(t("Описание канала", "Description")) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = NexusAccentCyan,
                            unfocusedBorderColor = Color(0xFF2C3B4E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (spaceName.isNotBlank()) {
                            viewModel.createNewSpace(spaceName, spaceDescription)
                            spaceName = ""
                            spaceDescription = ""
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_space")
                ) {
                    Text(t("Создать", "Create"), color = NexusAccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(t("Отмена", "Cancel"), color = NexusTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SpaceCardItem(
    space: SpaceEntity,
    onJoinToggle: () -> Unit,
    onOpen: () -> Unit,
    isEn: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (space.isJoined) onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        border = if (space.isJoined) null else BorderStroke(1.dp, Color(0xFF1F2B39))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = space.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onJoinToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (space.isJoined) Color(0xFF233040) else NexusPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (space.isJoined) {
                            if (isEn) "Joined" else "Вступил"
                        } else {
                            if (isEn) "Join" else "Вступить"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (space.isJoined) NexusTextSecondary else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = space.description,
                color = NexusTextSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = NexusAccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${space.memberCount} участников",
                    color = NexusAccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (space.isJoined) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEn) "Open Chat" else "Открыть Чат",
                            color = NexusPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowRightAlt,
                            contentDescription = null,
                            tint = NexusPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

object BoxDefaults {
    @Composable
    fun borderWindow() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B39))
}

// -------------------------------------------------------------
// SEARCH TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(
    viewModel: NexusViewModel,
    onNavigateToChat: (String) -> Unit,
    user: LocalUserEntity
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isEn = user.appLanguage == "EN"
    fun t(ru: String, en: String) = if (isEn) en else ru

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(t("Поиск в NEXUS", "Global Search"), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchUsers(it) },
            placeholder = { Text(t("Введите имя или @юзернейм...", "Enter name or @username...")) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NexusPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = NexusAccentCyan,
                unfocusedBorderColor = Color(0xFF2C3B4E)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = NexusTextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.trim().isEmpty()) {
                            t("Найдите Павла Дурова, Илона Маска или любого друга", "Search Pavel Durov, Elon Musk, or any friend")
                        } else {
                            t("Результатов не найдено. Нажмите, чтобы написать @$searchQuery", "No matched search. Press to launch chat @$searchQuery")
                        },
                        color = NexusTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(searchResults) { partner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectChatPartner(partner.username)
                                onNavigateToChat(partner.username)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(partner.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (partner.displayName.firstOrNull() ?: '@').toString().uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partner.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = partner.username,
                                    color = NexusAccentCyan,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = partner.bio,
                                color = NexusTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = t("Начать чат", "Start chat"),
                            tint = NexusPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PROFILE TAB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    viewModel: NexusViewModel,
    user: LocalUserEntity
) {
    val isEn = user.appLanguage == "EN"
    fun t(ru: String, en: String) = if (isEn) en else ru

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopAppBar(
                title = { Text(t("Профиль и Подписки", "Profile & VIP Club"), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
            )
        }

        // 1. User Info Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(user.avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.displayName.firstOrNull() ?: '@').toString().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            // Visual Badge based on subscription
                            val (badgeText, badgeColor) = when (user.subscriptionType) {
                                "Nexus+" -> Pair("NEXUS+", NexusPlusColor)
                                "Nexus Max" -> Pair("MAX ★", NexusMaxColor)
                                else -> Pair("Free", NexusTextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor
                                )
                            }
                        }

                        Text(
                            text = user.username,
                            color = NexusAccentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. Select Language Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NexusAccentCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = t("Язык интерфейса", "Interface Language"),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row {
                        Button(
                            onClick = { viewModel.setLanguage("RU") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.appLanguage == "RU") NexusPrimary else Color(0xFF233040)
                            ),
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("RU", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.setLanguage("EN") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.appLanguage == "EN") NexusPrimary else Color(0xFF233040)
                            ),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("EN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 3. System VIP Subscription Options
        item {
            Text(
                text = t("ВЫБОР ПОДПИСКИ (MVP ТАРИФЫ)", "STATION SUBSCRIPTION PLANS (MVP TIER)"),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NexusTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Free card
                SubscriptionTierCard(
                    title = "Nexus Free",
                    price = t("Бесплатно", "Free"),
                    benefits = listOf(
                        t("Базовые лимиты символов", "Basic character limits"),
                        t("Открытые Пространства", "Open public spaces")
                    ),
                    selected = user.subscriptionType == "Free",
                    accentColor = NexusPrimary,
                    onClick = { viewModel.updateSubscription("Free") }
                )

                // Plus Card
                SubscriptionTierCard(
                    title = "Nexus +",
                    price = "$4.99 / " + t("мес", "mo"),
                    benefits = listOf(
                        t("Специальный статус в чате", "Special purple VIP badge"),
                        t("Приоритетное создание Пространств", "Priority creation of spaces"),
                        t("Поддержка 24/7 от Nexus Support", "24/7 Official Support Priority")
                    ),
                    selected = user.subscriptionType == "Nexus+",
                    accentColor = NexusPlusColor,
                    onClick = { viewModel.updateSubscription("Nexus+") }
                )

                // Max Card
                SubscriptionTierCard(
                    title = "Nexus MAX ★",
                    price = "$9.99 / " + t("мес", "mo"),
                    benefits = listOf(
                        t("Эксклюзивная золотая звезда", "Exclusive gold star indicator"),
                        t("Умные моментальные ответы Дурова", "Instant priority Durov/Musk mockups"),
                        t("Эксклюзивные реакции и анимации", "Skins, custom items & animations")
                    ),
                    selected = user.subscriptionType == "Nexus Max",
                    accentColor = NexusMaxColor,
                    onClick = { viewModel.updateSubscription("Nexus Max") }
                )
            }
        }

        // logout
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("Выйти из системы NEXUS", "Logout from NEXUS"), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SubscriptionTierCard(
    title: String,
    price: String,
    benefits: List<String>,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accentColor else Color(0xFF1E2A38),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = NexusTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = price,
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 32.dp)
            ) {
                benefits.forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = benefit,
                            color = NexusTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

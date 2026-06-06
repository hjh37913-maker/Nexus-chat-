package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class NexusRepository(private val database: NexusDatabase) {
    private val dao = database.nexusDao()

    fun getLocalUser(): Flow<LocalUserEntity?> = dao.getLocalUserFlow()

    suspend fun getLocalUserDirect() = dao.getLocalUserDirect()

    suspend fun saveLocalUser(user: LocalUserEntity) = dao.saveLocalUser(user)

    suspend fun clearLocalUser() = dao.clearLocalUser()

    fun getAllPartners(): Flow<List<ChatPartnerEntity>> = dao.getAllPartnersFlow()

    suspend fun getPartner(username: String) = dao.getPartnerByUsername(username)

    suspend fun insertPartner(partner: ChatPartnerEntity) = dao.insertPartner(partner)

    suspend fun searchPartners(query: String): List<ChatPartnerEntity> {
        val cleanQuery = if (query.startsWith("@")) query else "@$query"
        val dbResults = dao.searchPartners("%${query.replace("@", "")}%")
        if (dbResults.isNotEmpty()) return dbResults
        
        // If query doesn't match and has reasonable length, dynamically suggest/add them as chat partner
        val stripped = cleanQuery.removePrefix("@")
        if (stripped.length >= 3) {
            val generated = ChatPartnerEntity(
                username = cleanQuery,
                displayName = stripped.replaceFirstChar { it.uppercase() },
                avatarColor = stripped.hashCode().coerceAtLeast(0),
                bio = "Пользователь NEXUS",
                customPhraseType = "ALICE"
            )
            return listOf(generated)
        }
        return emptyList()
    }

    fun getMessagesForPartner(partnerUsername: String): Flow<List<MessageEntity>> = dao.getMessagesForPartner(partnerUsername)

    suspend fun markMessagesAsRead(partnerUsername: String) = dao.markMessagesAsRead(partnerUsername)

    suspend fun insertMessage(message: MessageEntity) = dao.insertMessage(message)

    fun getAllMessagesFlow(): Flow<List<MessageEntity>> = dao.getAllMessagesFlow()

    fun getAllSpaces(): Flow<List<SpaceEntity>> = dao.getAllSpacesFlow()

    suspend fun createSpace(space: SpaceEntity) = dao.insertSpace(space)

    suspend fun toggleSpaceJoin(spaceId: String, currentJoinedState: Boolean) {
        val countDiff = if (currentJoinedState) -1 else 1
        dao.updateSpaceJoinState(spaceId, !currentJoinedState, countDiff)
    }

    fun getSpaceMessages(spaceId: String): Flow<List<SpaceMessageEntity>> = dao.getSpaceMessages(spaceId)

    suspend fun insertSpaceMessage(msg: SpaceMessageEntity) = dao.insertSpaceMessage(msg)

    // Pre-populate database with cool mock accounts and channels
    suspend fun checkAndPrepopulate() {
        // Always populate partners if empty
        val currentPartners = dao.getAllPartnersFlow().firstOrNull() ?: emptyList()
        if (currentPartners.isEmpty()) {
            val mockPartners = listOf(
                ChatPartnerEntity(
                    username = "@durov",
                    displayName = "Павел Дуров",
                    avatarColor = 0xFF24A1DE.toInt(), // TG Blueish
                    bio = "NEXUS is safe. Privacy is not for sale.",
                    customPhraseType = "DUROV"
                ),
                ChatPartnerEntity(
                    username = "@elon",
                    displayName = "Elon Musk",
                    avatarColor = 0xFF1B1B1B.toInt(), // Space X / X dark
                    bio = "Mars & Rockets. Coding Nexus client in Rust soon.",
                    customPhraseType = "ELON"
                ),
                ChatPartnerEntity(
                    username = "@nexus_support",
                    displayName = "NEXUS Support",
                    avatarColor = 0xFF00E5FF.toInt(), // Cyan
                    bio = "Официальная поддержка мессенджера NEXUS.",
                    customPhraseType = "SUPPORT"
                ),
                ChatPartnerEntity(
                    username = "@alice_designer",
                    displayName = "Алиса (Дизайнер)",
                    avatarColor = 0xFFFF4081.toInt(), // Pinkish
                    bio = "UI/UX Designer. Создаю пиксельные бабблы.",
                    customPhraseType = "ALICE"
                )
            )
            dao.insertPartners(mockPartners)
        }

        // Check spaces
        val currentSpaces = dao.getAllSpacesFlow().firstOrNull() ?: emptyList()
        if (currentSpaces.isEmpty()) {
            val mockSpaces = listOf(
                SpaceEntity(
                    spaceId = "general",
                    name = "🌐 Общий Чатик (General)",
                    description = "Главное пространство для общения всех пользователей NEXUS. Обсуждаем всё на свете!",
                    memberCount = 1420,
                    isJoined = true
                ),
                SpaceEntity(
                    spaceId = "nexus_plus_club",
                    name = "✨ NEXUS+ Elite Lounge",
                    description = "Эксклюзивная группа для обладателей подписки Nexus+ и Nexus Max. Обсуждение инсайдов и бета-функций.",
                    memberCount = 89,
                    isJoined = false
                ),
                SpaceEntity(
                    spaceId = "crypto_space",
                    name = "💎 TON & Crypto Hub",
                    description = "Всё о криптовалютах, блокчейне, безопасности и Web3 в экосистеме NEXUS.",
                    memberCount = 455,
                    isJoined = false
                ),
                SpaceEntity(
                    spaceId = "flutter_vs_native",
                    name = "📱 Mobile Dev Debate",
                    description = "Flutter vs Native Kotlin. Споры, бенчмарки, архитектурные паттерны и UI-гайды.",
                    memberCount = 210,
                    isJoined = true
                )
            )
            dao.insertSpaces(mockSpaces)

            // Add starter messages to spaces
            dao.insertSpaceMessage(SpaceMessageEntity(
                spaceId = "general",
                senderUsername = "@durov",
                senderDisplayName = "Павел Дуров",
                senderAvatarColor = 0xFF24A1DE.toInt(),
                content = "Добро пожаловать в NEXUS! Это новое поколение мессенджеров.",
                timestamp = System.currentTimeMillis() - 3600000 * 2
            ))
            dao.insertSpaceMessage(SpaceMessageEntity(
                spaceId = "general",
                senderUsername = "@elon",
                senderDisplayName = "Elon Musk",
                senderAvatarColor = 0xFF1B1B1B.toInt(),
                content = "This app UI is incredibly fast. Jetpack Compose rules!",
                timestamp = System.currentTimeMillis() - 3600000
            ))
            dao.insertSpaceMessage(SpaceMessageEntity(
                spaceId = "flutter_vs_native",
                senderUsername = "@alice_designer",
                senderDisplayName = "Алиса (Дизайнер)",
                senderAvatarColor = 0xFFFF4081.toInt(),
                content = "Ребята, Flutter классный для быстрых набросков, но нативный Jetpack Compose даёт невероятно плавную 120 FPS анимацию!",
                timestamp = System.currentTimeMillis() - 3600000
            ))
        }
    }
}

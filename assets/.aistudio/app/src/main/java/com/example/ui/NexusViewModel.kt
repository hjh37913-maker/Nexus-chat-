package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Represents an active chat with message summary
data class ActiveChat(
    val partner: ChatPartnerEntity,
    val lastMessage: MessageEntity?
)

class NexusViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NexusRepository

    init {
        val database = NexusDatabase.getDatabase(application)
        repository = NexusRepository(database)
        
        // Initialize prepopulation in background
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    // Local user profile state
    val localUser: StateFlow<LocalUserEntity?> = repository.getLocalUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All available spaces
    val spaces: StateFlow<List<SpaceEntity>> = repository.getAllSpaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All partners
    val allPartners: StateFlow<List<ChatPartnerEntity>> = repository.getAllPartners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search results state
    private val _searchResults = MutableStateFlow<List<ChatPartnerEntity>>(emptyList())
    val searchResults: StateFlow<List<ChatPartnerEntity>> = _searchResults.asStateFlow()

    // Combined active chats flow (ordered by last message timestamp)
    val activeChats: StateFlow<List<ActiveChat>> = combine(
        repository.getAllPartners(),
        repository.getAllMessagesFlow()
    ) { partners, allMessages ->
        val partnersMap = partners.associateBy { it.username }
        
        // Group messages by partnerUsername, find last message
        val lastMessagesMap = allMessages
            .groupBy { it.partnerUsername }
            .mapValues { (_, msgs) -> msgs.maxByOrNull { it.timestamp } }

        // We want to show partners that either have messages OR we explicitly want to display in the primary chat list
        // Let's list partners who have messages, or list Durov/Support by default as onboarding chats
        val activeUsernameList = lastMessagesMap.keys.toMutableSet()
        activeUsernameList.add("@durov")
        activeUsernameList.add("@nexus_support")

        activeUsernameList.mapNotNull { username ->
            val partner = partnersMap[username] ?: ChatPartnerEntity(
                username = username,
                displayName = username.removePrefix("@").replaceFirstChar { it.uppercase() },
                avatarColor = username.hashCode(),
                bio = "Пользователь NEXUS",
                customPhraseType = "ALICE"
            )
            val lastMsg = lastMessagesMap[username]
            ActiveChat(partner, lastMsg)
        }.sortedByDescending { it.lastMessage?.timestamp ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User authentication/registration
    fun registerUser(username: String, displayName: String) {
        viewModelScope.launch {
            val cleanUsername = if (username.startsWith("@")) username else "@$username"
            val newUser = LocalUserEntity(
                username = cleanUsername,
                displayName = displayName,
                subscriptionType = "Free",
                avatarColor = cleanUsername.hashCode()
            )
            repository.saveLocalUser(newUser)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearLocalUser()
        }
    }

    fun updateSubscription(type: String) {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            val updated = currentUser.copy(subscriptionType = type)
            repository.saveLocalUser(updated)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val currentUser = localUser.value ?: return@launch
            val updated = currentUser.copy(appLanguage = lang)
            repository.saveLocalUser(updated)
        }
    }

    fun searchUsers(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = repository.searchPartners(query)
            _searchResults.value = results
        }
    }

    // Flow for current open chat Messages
    private val _currentChatPartnerUsername = MutableStateFlow<String?>(null)
    val currentChatPartnerUsername: StateFlow<String?> = _currentChatPartnerUsername.asStateFlow()

    val currentChatMessages: StateFlow<List<MessageEntity>> = _currentChatPartnerUsername
        .flatMapLatest { username ->
            if (username == null) flowOf(emptyList())
            else repository.getMessagesForPartner(username)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectChatPartner(username: String?) {
        _currentChatPartnerUsername.value = username
        if (username != null) {
            viewModelScope.launch {
                repository.markMessagesAsRead(username)
                // If partner is not in DB yet, insert it from search
                val partnerInDb = repository.getPartner(username)
                if (partnerInDb == null) {
                    val fallbackDisplayName = username.removePrefix("@").replaceFirstChar { it.uppercase() }
                    repository.insertPartner(
                        ChatPartnerEntity(
                            username = username,
                            displayName = fallbackDisplayName,
                            avatarColor = username.hashCode(),
                            bio = "Новый контакт NEXUS",
                            customPhraseType = "ALICE"
                        )
                    )
                }
            }
        }
    }

    // Send a message
    fun sendPrivateMessage(text: String) {
        val partner = _currentChatPartnerUsername.value ?: return
        val sender = localUser.value?.username ?: "@me"
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            val myMsg = MessageEntity(
                partnerUsername = partner,
                senderUsername = sender,
                content = text,
                isRead = true
            )
            repository.insertMessage(myMsg)

            // Trigger simulated response
            simulatePartnerReply(partner, text)
        }
    }

    private fun simulatePartnerReply(partnerUsername: String, userText: String) {
        viewModelScope.launch {
            delay(1200) // typing simulation
            val partner = repository.getPartner(partnerUsername) ?: return@launch
            val responseStyle = partner.customPhraseType
            val lang = localUser.value?.appLanguage ?: "RU"

            val replyContent = formulateSmartReply(responseStyle, userText, lang)
            val replyMsg = MessageEntity(
                partnerUsername = partnerUsername,
                senderUsername = partnerUsername,
                content = replyContent,
                isRead = false
            )
            repository.insertMessage(replyMsg)
        }
    }

    private fun formulateSmartReply(style: String, input: String, lang: String): String {
        val isEn = lang == "EN"
        return when (style) {
            "DUROV" -> {
                val replies = if (isEn) {
                    listOf(
                        "Your privacy and freedom of expression are fully secure on NEXUS. No backdoors.",
                        "Our architecture handles gigabytes of secure decentralized data flawlessly.",
                        "Unlike other silicon valley messengers, NEXUS will never share your chats with advertisers.",
                        "We stand for freedom. Thank you for utilizing the fully sovereign NEXUS app.",
                        "This layout is beautiful. Simple, fast, and secure. That represents our design code."
                    )
                } else {
                    listOf(
                        "Ваша конфиденциальность и свобода выражения находятся под абсолютной защитой NEXUS. Никаких бэкдоров.",
                        "Наша архитектура безупречно справляется с гигабайтами зашифрованных данных.",
                        "В отличие от корпоративных мессенджеров, NEXUS никогда не передаст ваши чаты рекламодателям.",
                        "Мы выступаем за цифровую свободу. Спасибо за использование суверенного приложения NEXUS.",
                        "Интерфейс NEXUS прекрасен. Простой, быстрый и безопасный. Это наш эталон качества."
                    )
                }
                replies[Random.nextInt(replies.size)]
            }
            "ELON" -> {
                val replies = if (isEn) {
                    listOf(
                        "Interesting indeed. Can we launch these message bubbles directly to Mars?",
                        "The next SpaceX Starlink flight will verify secure NEXUS orbital link capabilities.",
                        "Excellent code execution. I am thinking about integrating Twitter/X posts with NEXUS nodes.",
                        "Kotlin and Compose is super high performance. Have you tried writing your systems in Rust?",
                        "To the moon! We must scale NEXUS servers with high-density battery backups."
                    )
                } else {
                    listOf(
                        "Интересно! Можем ли мы отправить этот мессенджер прямиком на Марс?",
                        "Следующий запуск Starlink проверит орбитальную передачу данных для NEXUS.",
                        "Отличная архитектура чата! Думаю интегрировать NEXUS-ноды с серверами X.",
                        "Kotlin разработка — это супер. Но ты когда-нибудь писал на высокоэффективном Rust?",
                        "Вперед на Луну! Нам нужно резервировать дата-центры NEXUS с помощью Tesla Megapack."
                    )
                }
                replies[Random.nextInt(replies.size)]
            }
            "SUPPORT" -> {
                val replies = if (isEn) {
                    listOf(
                        "Greeting from NEXUS HQ! Thank you for testing out MVP v1.0. Let us know of your suggestions.",
                        "You can access premium chat aesthetics, customizable avatars, and high-priority reactions by subscribing to NEXUS+ in your Profile.",
                        "NEXUS runs entirely on local SQLite Room database, meaning zero-knowledge and offline resilience.",
                        "Need further help? Feel free to create custom community Spaces to chat with friends!"
                    )
                } else {
                    listOf(
                        "Приветствуем от лица команды NEXUS! Спасибо за тестирование MVP v1.0. Расскажите о ваших идеях!",
                        "Вы можете получить доступ к премиальным реакциям, неограниченным стикерам и золотым ярлыкам, оформив Nexus+ в профиле.",
                        "NEXUS полностью сохраняет данные в локальной СУБД Room. Это гарантирует полную суверенность переписки.",
                        "Нужна помощь? Вы можете бесплатно создавать новые Пространства (Spaces) во второй вкладке!"
                    )
                }
                replies[Random.nextInt(replies.size)]
            }
            else -> { // ALICE / BOB / General Users
                val replies = if (isEn) {
                    listOf(
                        "Wow, this feels so smooth! The telegram-like dark styling is gorgeous.",
                        "How is your day going? Have you checked out the global spaces tab?",
                        "I really like these teal active indicators and checkmark badges.",
                        "I'm currently designing custom animated emoji packs for Nexus plus members!",
                        "Let's create a space together and invite some devs."
                    )
                } else {
                    listOf(
                        "Ого, списки работают так плавно! Тёмная тема в стиле Телеграм — просто пушка.",
                        "Как проходят твои дела? Заглядывал уже во вкладку Пространств?",
                        "Мне безумно нравятся бирюзовые акценты и пиксельные пузыри сообщений.",
                        "Сейчас как раз дорисовываю анимированные реакции для подписчиков Nexus Max!",
                        "Давай создадим новое Пространство и пригласим туда знакомых разработчиков?"
                    )
                }
                replies[Random.nextInt(replies.size)]
            }
        }
    }

    // Space details state
    private val _currentSpaceId = MutableStateFlow<String?>(null)
    val currentSpaceId: StateFlow<String?> = _currentSpaceId.asStateFlow()

    val currentSpaceMessages: StateFlow<List<SpaceMessageEntity>> = _currentSpaceId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getSpaceMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSpace(spaceId: String?) {
        _currentSpaceId.spaceValue = spaceId
    }

    private var MutableStateFlow<String?>.spaceValue: String?
        get() = value
        set(v) { value = v }

    fun joinOrLeaveSpace(space: SpaceEntity) {
        viewModelScope.launch {
            repository.toggleSpaceJoin(space.spaceId, space.isJoined)
        }
    }

    fun createNewSpace(name: String, description: String) {
        if (name.trim().isEmpty()) return
        val creator = localUser.value?.username ?: "@me"
        viewModelScope.launch {
            val spaceId = "custom_${System.currentTimeMillis()}"
            val newSpace = SpaceEntity(
                spaceId = spaceId,
                name = "🚀 $name",
                description = description,
                memberCount = 1,
                isJoined = true,
                isCustom = true
            )
            repository.createSpace(newSpace)
            
            // Add initial welcome message from user
            repository.insertSpaceMessage(SpaceMessageEntity(
                spaceId = spaceId,
                senderUsername = creator,
                senderDisplayName = localUser.value?.displayName ?: "Создатель",
                senderAvatarColor = (localUser.value?.avatarColor ?: 0xFF00E5FF.toInt()),
                content = "Добро пожаловать в наше новое Пространство: $name! 👋"
            ))
        }
    }

    fun sendSpaceMessage(text: String) {
        val sId = _currentSpaceId.value ?: return
        if (text.trim().isEmpty()) return
        val me = localUser.value ?: return

        viewModelScope.launch {
            val myMsg = SpaceMessageEntity(
                spaceId = sId,
                senderUsername = me.username,
                senderDisplayName = me.displayName,
                senderAvatarColor = me.avatarColor,
                content = text
            )
            repository.insertSpaceMessage(myMsg)

            // Trigger a dynamic community reply!
            simulateSpaceReply(sId, text)
        }
    }

    private fun simulateSpaceReply(spaceId: String, userText: String) {
        viewModelScope.launch {
            delay(1500)
            val lang = localUser.value?.appLanguage ?: "RU"
            val senderInfo = getRandomCommunityMember()

            val response = if (lang == "EN") {
                listOf(
                    "Agreed! Completely support this.",
                    "Wait, can you explain this in more detail?",
                    "Haha indeed! Truly futuristic messenger.",
                    "Absolutely, Nexus community members should unite!",
                    "Is this channel fully client-side? SQLite persistence rules!"
                )[Random.nextInt(5)]
            } else {
                listOf(
                    "Полностью поддерживаю это мнение!",
                    "Интересный поинт. Расскажи подробнее?",
                    "Ха-ха, забавно! NEXUS действительно радует скоростью.",
                    "Я вступил сюда несколько дней назад, отличный чатик.",
                    "Действительно круто иметь оффлайн-режим через Room Database."
                )[Random.nextInt(5)]
            }

            val replyMsg = SpaceMessageEntity(
                spaceId = spaceId,
                senderUsername = senderInfo.first,
                senderDisplayName = senderInfo.second,
                senderAvatarColor = senderInfo.third,
                content = response
            )
            repository.insertSpaceMessage(replyMsg)
        }
    }

    private fun getRandomCommunityMember(): Triple<String, String, Int> {
        val members = listOf(
            Triple("@nikita_dev", "Никита Android", 0xFFE040FB.toInt()),
            Triple("@dasha_web", "Даша Верстает", 0xFFFF5252.toInt()),
            Triple("@vlad_crypto", "Vlad TON-holder", 0xFF448AFF.toInt()),
            Triple("@sergey_cto", "Сергей Капитанович", 0xFF4CAF50.toInt())
        )
        return members[Random.nextInt(members.size)]
    }
}

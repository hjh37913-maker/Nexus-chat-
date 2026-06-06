package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_user")
data class LocalUserEntity(
    @PrimaryKey val username: String, // e.g. "@username"
    val displayName: String,
    val subscriptionType: String, // "Free", "Nexus+", "Nexus Max"
    val avatarColor: Int,
    val appLanguage: String = "RU" // "RU", "EN"
)

@Entity(tableName = "chat_partners")
data class ChatPartnerEntity(
    @PrimaryKey val username: String, // e.g. "@durov"
    val displayName: String,
    val avatarColor: Int,
    val bio: String,
    val customPhraseType: String // "DUROV", "ELON", "SUPPORT", "ALICE", "BOB"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerUsername: String, // Who is the other person in this chat
    val senderUsername: String,  // Who sent this message (could be me or partner)
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "spaces")
data class SpaceEntity(
    @PrimaryKey val spaceId: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val isJoined: Boolean = false,
    val isCustom: Boolean = false // user-generated
)

@Entity(tableName = "space_messages")
data class SpaceMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spaceId: String,
    val senderUsername: String,
    val senderDisplayName: String,
    val senderAvatarColor: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface NexusDao {
    // Local user
    @Query("SELECT * FROM local_user LIMIT 1")
    fun getLocalUserFlow(): Flow<LocalUserEntity?>

    @Query("SELECT * FROM local_user LIMIT 1")
    suspend fun getLocalUserDirect(): LocalUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalUser(user: LocalUserEntity)

    @Query("DELETE FROM local_user")
    suspend fun clearLocalUser()

    // Partners
    @Query("SELECT * FROM chat_partners")
    fun getAllPartnersFlow(): Flow<List<ChatPartnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<ChatPartnerEntity>)

    @Query("SELECT * FROM chat_partners WHERE username = :username LIMIT 1")
    suspend fun getPartnerByUsername(username: String): ChatPartnerEntity?

    @Query("SELECT * FROM chat_partners WHERE username LIKE :query OR displayName LIKE :query")
    suspend fun searchPartners(query: String): List<ChatPartnerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: ChatPartnerEntity)

    // Private messages
    @Query("SELECT * FROM messages WHERE partnerUsername = :partner ORDER BY timestamp ASC")
    fun getMessagesForPartner(partner: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isRead = 1 WHERE partnerUsername = :partner AND senderUsername = :partner")
    suspend fun markMessagesAsRead(partner: String)

    // Spaces
    @Query("SELECT * FROM spaces")
    fun getAllSpacesFlow(): Flow<List<SpaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: SpaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpaces(spaces: List<SpaceEntity>)

    @Query("UPDATE spaces SET isJoined = :joined, memberCount = memberCount + :countDiff WHERE spaceId = :spaceId")
    suspend fun updateSpaceJoinState(spaceId: String, joined: Boolean, countDiff: Int)

    // Space messages
    @Query("SELECT * FROM space_messages WHERE spaceId = :spaceId ORDER BY timestamp ASC")
    fun getSpaceMessages(spaceId: String): Flow<List<SpaceMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpaceMessage(msg: SpaceMessageEntity)
}

@Database(
    entities = [
        LocalUserEntity::class,
        ChatPartnerEntity::class,
        MessageEntity::class,
        SpaceEntity::class,
        SpaceMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun nexusDao(): NexusDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

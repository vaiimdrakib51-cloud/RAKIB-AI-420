package com.nova.ai.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey val id: String,
    val text: String,
    val sender: String,
    val timestamp: Long
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}

@Database(entities = [ChatEntity::class], version = 1, exportSchema = false)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

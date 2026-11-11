package com.nova.ai.di

import android.content.Context
import androidx.room.Room
import com.nova.ai.data.local.ChatDao
import com.nova.ai.data.local.NovaDatabase
import com.nova.ai.data.repository.AIRepositoryImpl
import com.nova.ai.domain.repository.AIRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NovaDatabase {
        return Room.databaseBuilder(
            context,
            NovaDatabase::class.java,
            "nova_ai_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideChatDao(database: NovaDatabase): ChatDao {
        return database.chatDao()
    }
}

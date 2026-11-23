package com.finora.app.di

import android.content.Context
import androidx.room.Room
import com.finora.app.data.local.FinoraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinoraDatabase {
        return Room.databaseBuilder(
            context,
            FinoraDatabase::class.java,
            "finora_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
}

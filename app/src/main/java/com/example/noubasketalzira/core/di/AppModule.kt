package com.example.noubasketalzira.core.di

import androidx.room.Room
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.auth.MockSessionManager
import com.example.noubasketalzira.core.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "noubasket_db"
        ).build()
    }
    
    // Auth
    single<ISessionManager> { MockSessionManager() }
}

package com.example.noubasketalzira.core.di

import androidx.room.Room
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.auth.MockSessionManager
import com.example.noubasketalzira.core.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "noubasket_db"
        ).build()
    }
    single { get<AppDatabase>().teamDao() }
    single { get<AppDatabase>().teamMemberDao() }
    
    // Repositories
    single<com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository> {
        com.example.noubasketalzira.feature.teams.data.repository.TeamRepositoryImpl(get(), get())
    }
    
    // Auth
    single<ISessionManager> { MockSessionManager() }
    
    // ViewModels
    viewModel {
        com.example.noubasketalzira.feature.teams.ui.TeamViewModel(get(), get())
    }
}

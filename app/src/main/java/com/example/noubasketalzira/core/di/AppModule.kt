package com.example.noubasketalzira.core.di

import androidx.room.Room
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.auth.MockSessionManager
import com.example.noubasketalzira.core.data.local.AppDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module

val appModule = module {
    // Supabase Client
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = "https://hppxhwihxckexlayjejd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhwcHhod2loeGNrZXhsYXlqZWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc0MDcxNzgsImV4cCI6MjEwMjk4MzE3OH0.cDBZjMAUW6qnzYGJPz2Sp7rw3lUCt0K0PebZa_OZ-a0"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

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
        com.example.noubasketalzira.feature.teams.data.repository.TeamRepositoryImpl(get(), get(), get(), androidContext())
    }
    
    // Auth
    single<ISessionManager> { MockSessionManager() }
    
    // ViewModels
    viewModel {
        com.example.noubasketalzira.feature.teams.ui.TeamViewModel(get(), get())
    }
}

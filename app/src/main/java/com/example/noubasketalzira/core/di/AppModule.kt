package com.example.noubasketalzira.core.di

import androidx.room.Room
import com.example.noubasketalzira.core.auth.ISessionManager
import com.example.noubasketalzira.core.data.local.AppDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import com.example.noubasketalzira.core.domain.util.IIdGenerator
import com.example.noubasketalzira.core.framework.android.util.AndroidDateFormatter
import com.example.noubasketalzira.core.framework.android.util.AndroidIdGenerator
import com.example.noubasketalzira.core.framework.android.worker.AndroidSyncScheduler
import com.example.noubasketalzira.core.domain.scheduler.ISyncScheduler
import com.example.noubasketalzira.feature.events.data.repository.EventRepositoryImpl
import com.example.noubasketalzira.feature.events.data.source.local.EventLocalDataSourceImpl
import com.example.noubasketalzira.feature.events.data.source.local.IEventLocalDataSource
import com.example.noubasketalzira.feature.events.data.source.remote.EventRemoteDataSourceImpl
import com.example.noubasketalzira.feature.events.data.source.remote.IEventRemoteDataSource
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import com.example.noubasketalzira.feature.teams.data.repository.TeamRepositoryImpl
import com.example.noubasketalzira.feature.teams.data.source.local.ITeamLocalDataSource
import com.example.noubasketalzira.feature.teams.data.source.local.TeamLocalDataSourceImpl
import com.example.noubasketalzira.feature.teams.data.source.remote.ITeamRemoteDataSource
import com.example.noubasketalzira.feature.teams.data.source.remote.TeamRemoteDataSourceImpl
import com.example.noubasketalzira.feature.teams.domain.repository.ITeamRepository

import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer

val appModule = module {
    // Supabase Client
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = "https://hppxhwihxckexlayjejd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhwcHhod2loeGNrZXhsYXlqZWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc0MDcxNzgsImV4cCI6MjEwMjk4MzE3OH0.cDBZjMAUW6qnzYGJPz2Sp7rw3lUCt0K0PebZa_OZ-a0"
        ) {
            defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
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
    
    // Utilities
    single<com.example.noubasketalzira.core.domain.util.IDateFormatter> { com.example.noubasketalzira.core.framework.android.util.AndroidDateFormatter() }
    single<com.example.noubasketalzira.core.domain.util.IReportExporter> { com.example.noubasketalzira.core.framework.android.util.AndroidReportExporter(androidContext()) }
    single<com.example.noubasketalzira.core.domain.util.IFileSharer> { com.example.noubasketalzira.core.framework.android.util.AndroidFileSharer(androidContext()) }

    single { get<AppDatabase>().teamDao() }
    single { get<AppDatabase>().teamMemberDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().eventDao() }
    single { get<AppDatabase>().attendanceDao() }
    
    // Utilities
    single<IIdGenerator> { AndroidIdGenerator() }
    single<IDateFormatter> { AndroidDateFormatter() }

    // Scheduler
    single<ISyncScheduler> {
        AndroidSyncScheduler(androidContext())
    }
    
    // Data Sources
    single<ITeamLocalDataSource> {
        TeamLocalDataSourceImpl(get(), get(), get())
    }
    single<ITeamRemoteDataSource> {
        TeamRemoteDataSourceImpl(get())
    }
    
    single<IEventLocalDataSource> {
        EventLocalDataSourceImpl(get(), get(), get(), get())
    }
    single<IEventRemoteDataSource> {
        EventRemoteDataSourceImpl(get())
    }
    
    // Repositories
    single<ITeamRepository> {
        TeamRepositoryImpl(get(), get(), get(), get())
    }
    
    single<IEventRepository> {
        EventRepositoryImpl(get(), get(), get(), get(), get())
    }
    
    // UseCases
    factory { com.example.noubasketalzira.feature.events.domain.usecase.GenerateEventsReportUseCase(get(), get(), get(), get()) }
    
    // Auth
    single<com.example.noubasketalzira.core.auth.IAuthDataSource> { 
        com.example.noubasketalzira.core.auth.SupabaseAuthDataSource(get()) 
    }
    single<ISessionManager> { 
        com.example.noubasketalzira.core.auth.SessionManagerImpl(androidContext(), get(), get(), get(), get(), get()) 
    }
    
    single<com.example.noubasketalzira.feature.users.data.repository.IUserRepository> { 
        com.example.noubasketalzira.feature.users.data.repository.UserRepositoryImpl(get(), get()) 
    }
    
    // ViewModels
    viewModel {
        com.example.noubasketalzira.feature.auth.ui.LoginViewModel(get())
    }
    viewModel {
        com.example.noubasketalzira.feature.auth.ui.OtpViewModel(get())
    }
    viewModel {
        com.example.noubasketalzira.feature.users.ui.UserManagementViewModel(get())
    }
    viewModel {
        com.example.noubasketalzira.feature.teams.ui.TeamViewModel(get(), get(), get())
    }
    viewModel { params ->
        com.example.noubasketalzira.feature.events.ui.EventListViewModel(params.get(), get(), get(), get())
    }
    viewModel { params ->
        com.example.noubasketalzira.feature.events.ui.EventDetailViewModel(params.get(), get(), get())
    }
}

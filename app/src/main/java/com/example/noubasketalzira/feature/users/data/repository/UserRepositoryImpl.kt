package com.example.noubasketalzira.feature.users.data.repository

import com.example.noubasketalzira.core.data.local.dao.UserDao
import com.example.noubasketalzira.core.data.local.entity.UserEntity
import com.example.noubasketalzira.core.data.local.entity.toDomain
import com.example.noubasketalzira.core.domain.model.User
import com.example.noubasketalzira.core.domain.model.UserRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

import com.example.noubasketalzira.core.domain.util.IIdGenerator

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val full_name: String,
    val role: String
)

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val supabase: SupabaseClient,
    private val idGenerator: IIdGenerator
) : IUserRepository {

    override fun observeUsers(): Flow<List<User>> {
        return userDao.observeAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createUser(email: String, fullName: String, role: UserRole) {
        withContext(Dispatchers.IO) {
            val dto = UserDto(
                id = idGenerator.generateUniqueId(),
                email = email,
                full_name = fullName,
                role = role.name
            )
            
            supabase.postgrest["users"].insert(dto)
            
            // Sync locally for UDF SSOT
            syncUsers()
        }
    }

    override suspend fun updateUser(userId: String, email: String, fullName: String, role: UserRole) {
        withContext(Dispatchers.IO) {
            val dto = UserDto(
                id = userId,
                email = email,
                full_name = fullName,
                role = role.name
            )
            
            supabase.postgrest["users"].update(dto) {
                filter { eq("id", userId) }
            }
            
            syncUsers()
        }
    }

    override suspend fun deleteUser(userId: String) {
        withContext(Dispatchers.IO) {
            supabase.postgrest["users"].delete {
                filter { eq("id", userId) }
            }
            userDao.deleteUser(userId)
        }
    }

    override suspend fun syncUsers() {
        withContext(Dispatchers.IO) {
            try {
                val remoteUsers = supabase.postgrest["users"].select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.list("id,email,full_name,role")
                ).decodeList<UserDto>()
                
                remoteUsers.forEach { dto ->
                    userDao.insertUser(
                        UserEntity(
                            id = dto.id,
                            email = dto.email,
                            fullName = dto.full_name,
                            role = UserRole.valueOf(dto.role)
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore sync errors when offline
            }
        }
    }
}

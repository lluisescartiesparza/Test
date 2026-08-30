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

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val full_name: String,
    val role: String
)

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val supabase: SupabaseClient
) : IUserRepository {

    override fun observeUsers(): Flow<List<User>> {
        return userDao.observeAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createUser(email: String, fullName: String, role: UserRole) {
        withContext(Dispatchers.IO) {
            // Note: In a real app, you would use Supabase Admin API to create the auth user
            // and trigger an invite email. Since Admin API shouldn't be on the client,
            // we will insert the profile into public.users and rely on an edge function 
            // or trigger to handle the auth side, or we just insert the public profile.
            // For now, we simulate inserting into public.users.
            
            val dto = UserDto(
                id = java.util.UUID.randomUUID().toString(),
                email = email,
                full_name = fullName,
                role = role.name
            )
            
            supabase.postgrest["users"].insert(dto)
            
            // Sync locally for UDF SSOT
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
                android.util.Log.e("NouBasketAuth", "syncUsers: Fetching from Supabase")
                val remoteUsers = supabase.postgrest["users"].select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.list("id,email,full_name,role")
                ).decodeList<UserDto>()
                android.util.Log.e("NouBasketAuth", "syncUsers: Fetched ${remoteUsers.size} users")
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
                android.util.Log.e("NouBasketAuth", "syncUsers: Insert completed")
            } catch (e: Exception) {
                android.util.Log.e("NouBasketAuth", "syncUsers: FAILED", e)
                // Ignore sync errors when offline
            }
        }
    }
}

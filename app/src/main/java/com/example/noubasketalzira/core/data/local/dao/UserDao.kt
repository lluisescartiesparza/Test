package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.noubasketalzira.core.data.local.entity.UserEntity

@Dao
interface UserDao {
    @androidx.room.Upsert
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeAllUsers(): kotlinx.coroutines.flow.Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    fun deleteUser(userId: String)
}

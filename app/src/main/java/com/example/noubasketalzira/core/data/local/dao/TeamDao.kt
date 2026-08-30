package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun observeAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): List<TeamEntity>

    @androidx.room.Upsert
    fun insertTeam(team: TeamEntity)

    @Query("DELETE FROM teams WHERE id = :teamId")
    fun deleteTeam(teamId: String)
    
    // For assigning a coach/player we would need TeamMemberDao, 
    // but for now we can just add a query if needed or handle it in another DAO.
}

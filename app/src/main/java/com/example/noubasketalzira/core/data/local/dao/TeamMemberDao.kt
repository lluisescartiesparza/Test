package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity

@Dao
interface TeamMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeamMember(member: TeamMemberEntity)

    @androidx.room.Query("SELECT * FROM team_members WHERE teamId = :teamId AND role = :role")
    fun getMembersByTeamIdAndRole(teamId: String, role: String): List<TeamMemberEntity>

    @androidx.room.Query("SELECT * FROM team_members WHERE userId = :userId")
    fun getMembersByUserId(userId: String): List<TeamMemberEntity>
}

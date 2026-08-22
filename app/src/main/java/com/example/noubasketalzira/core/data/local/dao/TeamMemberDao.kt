package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity

@Dao
interface TeamMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeamMember(member: TeamMemberEntity)
}

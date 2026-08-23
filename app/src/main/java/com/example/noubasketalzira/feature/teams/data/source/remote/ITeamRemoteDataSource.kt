package com.example.noubasketalzira.feature.teams.data.source.remote

import com.example.noubasketalzira.core.data.remote.dto.TeamDto
import com.example.noubasketalzira.core.data.remote.dto.TeamMemberDto
import com.example.noubasketalzira.core.data.remote.dto.UserDto

interface ITeamRemoteDataSource {
    suspend fun fetchTeams(): List<TeamDto>
    suspend fun fetchUsers(): List<UserDto>
    suspend fun fetchTeamMembers(): List<TeamMemberDto>
}

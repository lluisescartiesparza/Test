package com.example.noubasketalzira.feature.teams.data.source.remote

import com.example.noubasketalzira.core.data.remote.dto.TeamDto
import com.example.noubasketalzira.core.data.remote.dto.TeamMemberDto
import com.example.noubasketalzira.core.data.remote.dto.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class TeamRemoteDataSourceImpl(private val supabase: SupabaseClient) : ITeamRemoteDataSource {
    override suspend fun fetchTeams(): List<TeamDto> {
        return supabase.postgrest["teams"].select().decodeList<TeamDto>()
    }

    override suspend fun fetchUsers(): List<UserDto> {
        return supabase.postgrest["users"].select().decodeList<UserDto>()
    }

    override suspend fun fetchTeamMembers(): List<TeamMemberDto> {
        return supabase.postgrest["team_members"].select().decodeList<TeamMemberDto>()
    }
}

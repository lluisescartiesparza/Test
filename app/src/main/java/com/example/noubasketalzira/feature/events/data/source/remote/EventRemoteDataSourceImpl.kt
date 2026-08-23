package com.example.noubasketalzira.feature.events.data.source.remote

import com.example.noubasketalzira.feature.events.data.worker.AttendanceInsertDto
import com.example.noubasketalzira.feature.events.data.worker.EventInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class EventRemoteDataSourceImpl(private val supabase: SupabaseClient) : IEventRemoteDataSource {
    override suspend fun fetchEvents(teamId: String): List<EventInsertDto> {
        return supabase.postgrest["events"]
            .select { filter { eq("team_id", teamId) } }
            .decodeList<EventInsertDto>()
    }

    override suspend fun fetchAttendance(eventId: String): List<AttendanceInsertDto> {
        return supabase.postgrest["attendance"]
            .select { filter { eq("event_id", eventId) } }
            .decodeList<AttendanceInsertDto>()
    }
}

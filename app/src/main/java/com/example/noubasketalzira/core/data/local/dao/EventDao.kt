package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.noubasketalzira.core.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @androidx.room.Upsert
    fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE teamId = :teamId ORDER BY date DESC")
    fun observeEventsByTeam(teamId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: String): EventEntity?

    @Query("DELETE FROM events WHERE id = :eventId")
    fun deleteEvent(eventId: String)
}

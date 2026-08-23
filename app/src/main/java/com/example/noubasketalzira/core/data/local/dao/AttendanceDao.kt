package com.example.noubasketalzira.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.noubasketalzira.core.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @androidx.room.Upsert
    fun insertAttendance(attendance: AttendanceEntity)

    @androidx.room.Upsert
    fun insertAttendances(attendances: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance WHERE eventId = :eventId")
    fun observeAttendanceByEvent(eventId: String): Flow<List<AttendanceEntity>>

    @Query("UPDATE attendance SET status = :status WHERE eventId = :eventId AND userId = :userId")
    fun updateAttendanceStatus(eventId: String, userId: String, status: String)
    
    @Query("UPDATE attendance SET status = :status WHERE eventId = :eventId")
    fun updateAllAttendanceStatus(eventId: String, status: String)

    data class AttendanceWithUser(
        val eventId: String,
        val userId: String,
        val userName: String,
        val status: String
    )

    @Query("""
        SELECT 
            :eventId as eventId,
            u.id as userId,
            u.fullName as userName,
            COALESCE(a.status, 'NO_CONVOCADO') as status
        FROM team_members tm
        JOIN users u ON tm.userId = u.id
        LEFT JOIN attendance a ON a.userId = u.id AND a.eventId = :eventId
        WHERE tm.teamId = (SELECT teamId FROM events WHERE id = :eventId)
    """)
    fun observeAllTeamAttendances(eventId: String): Flow<List<AttendanceWithUser>>
}

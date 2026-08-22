package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class EventType {
    ENTRENAMIENTO, PARTIDO, TECNIFICACION
}

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teamId")]
)
data class EventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val teamId: String,
    val type: EventType,
    val date: Long, // timestamp
    val description: String?,
    val createdAt: Long = System.currentTimeMillis()
)

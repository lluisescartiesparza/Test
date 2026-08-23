package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

import com.example.noubasketalzira.feature.events.domain.model.EventType

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

fun EventEntity.toDomain(): com.example.noubasketalzira.feature.events.domain.model.Event {
    return com.example.noubasketalzira.feature.events.domain.model.Event(
        id = this.id,
        teamId = this.teamId,
        type = this.type,
        date = this.date,
        description = this.description
    )
}

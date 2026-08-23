package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.noubasketalzira.feature.teams.domain.model.Team

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String?,
    val createdAt: Long = System.currentTimeMillis()
)

fun TeamEntity.toDomain(): Team {
    return Team(
        id = id,
        name = name,
        category = category
    )
}

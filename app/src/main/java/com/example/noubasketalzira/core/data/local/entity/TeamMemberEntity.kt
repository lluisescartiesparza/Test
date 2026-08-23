package com.example.noubasketalzira.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "team_members",
    primaryKeys = ["teamId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("teamId")]
)
data class TeamMemberEntity(
    val teamId: String,
    val userId: String,
    val role: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun TeamMemberEntity.toDomain(): com.example.noubasketalzira.feature.teams.domain.model.TeamMember {
    return com.example.noubasketalzira.feature.teams.domain.model.TeamMember(
        teamId = this.teamId,
        userId = this.userId,
        role = com.example.noubasketalzira.feature.teams.domain.model.TeamRole.valueOf(this.role)
    )
}

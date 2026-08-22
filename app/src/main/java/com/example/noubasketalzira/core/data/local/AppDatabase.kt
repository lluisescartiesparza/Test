package com.example.noubasketalzira.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.noubasketalzira.core.data.local.entity.AttendanceEntity
import com.example.noubasketalzira.core.data.local.entity.EventEntity
import com.example.noubasketalzira.core.data.local.entity.TeamEntity
import com.example.noubasketalzira.core.data.local.entity.TeamMemberEntity
import com.example.noubasketalzira.core.data.local.entity.UserEntity

import com.example.noubasketalzira.core.data.local.dao.TeamDao
import com.example.noubasketalzira.core.data.local.dao.TeamMemberDao

@Database(
    entities = [
        UserEntity::class,
        TeamEntity::class,
        TeamMemberEntity::class,
        EventEntity::class,
        AttendanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun teamMemberDao(): TeamMemberDao
}

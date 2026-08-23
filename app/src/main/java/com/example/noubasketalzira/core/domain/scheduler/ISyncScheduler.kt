package com.example.noubasketalzira.core.domain.scheduler

interface ISyncScheduler {
    fun scheduleTeamSync(action: String, teamId: String, name: String? = null, category: String? = null)
    fun scheduleEventSync(action: String, eventId: String)
    fun scheduleAttendanceSync(eventId: String, userId: String?)
}

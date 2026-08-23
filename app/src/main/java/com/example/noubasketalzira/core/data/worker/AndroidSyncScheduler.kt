package com.example.noubasketalzira.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.noubasketalzira.core.domain.scheduler.ISyncScheduler
import com.example.noubasketalzira.feature.events.data.worker.AttendanceSyncWorker
import com.example.noubasketalzira.feature.events.data.worker.EventSyncWorker
import com.example.noubasketalzira.feature.teams.data.worker.TeamSyncWorker

class AndroidSyncScheduler(private val context: Context) : ISyncScheduler {

    override fun scheduleTeamSync(action: String, teamId: String, name: String?, category: String?) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString(TeamSyncWorker.KEY_ACTION, action)
            .putString(TeamSyncWorker.KEY_TEAM_ID, teamId)
            
        if (name != null) data.putString(TeamSyncWorker.KEY_TEAM_NAME, name)
        if (category != null) data.putString(TeamSyncWorker.KEY_TEAM_CATEGORY, category)
        
        val request = OneTimeWorkRequestBuilder<TeamSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override fun scheduleEventSync(action: String, eventId: String) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("action", action)
            .putString("eventId", eventId)
            
        val request = OneTimeWorkRequestBuilder<EventSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }

    override fun scheduleAttendanceSync(eventId: String, userId: String?) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val data = Data.Builder()
            .putString("eventId", eventId)
            
        if (userId != null) data.putString("userId", userId)
        
        val request = OneTimeWorkRequestBuilder<AttendanceSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()
            
        WorkManager.getInstance(context).enqueue(request)
    }
}

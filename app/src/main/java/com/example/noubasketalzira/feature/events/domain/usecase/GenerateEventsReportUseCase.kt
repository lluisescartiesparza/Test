package com.example.noubasketalzira.feature.events.domain.usecase

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import com.example.noubasketalzira.core.domain.util.IFileSharer
import com.example.noubasketalzira.core.domain.util.IReportExporter
import com.example.noubasketalzira.core.domain.util.ReportTable
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import com.example.noubasketalzira.feature.events.domain.model.EventType
import com.example.noubasketalzira.feature.events.domain.model.AttendanceStatus
import kotlinx.coroutines.flow.first

class GenerateEventsReportUseCase(
    private val repository: IEventRepository,
    private val exporter: IReportExporter,
    private val fileSharer: IFileSharer,
    private val dateFormatter: IDateFormatter
) {
    suspend operator fun invoke(
        teamId: String, 
        format: String,
        eventType: EventType? = null,
        fromDateMillis: Long? = null,
        toDateMillis: Long? = null
    ) {
        var events = repository.observeEvents(teamId).first()
        
        if (eventType != null) {
            events = events.filter { it.type == eventType }
        }
        if (fromDateMillis != null) {
            events = events.filter { it.date >= fromDateMillis }
        }
        if (toDateMillis != null) {
            events = events.filter { it.date <= toDateMillis }
        }
        events = events.sortedBy { it.date }
        
        val attendancesByEvent = mutableMapOf<String, List<com.example.noubasketalzira.feature.events.domain.model.Attendance>>()
        val allPlayerNames = mutableSetOf<String>()
        
        for (event in events) {
            val attendances = repository.observeAttendance(event.id).first()
            attendancesByEvent[event.id] = attendances
            attendances.forEach { allPlayerNames.add(it.userName) }
        }
        
        val sortedPlayers = allPlayerNames.sorted()
        val isPdf = format.lowercase() == "pdf"
        
        fun formatText(text: String, limit: Int = 8): String {
            if (!isPdf) return text
            return if (text.length > limit) text.substring(0, limit) else text
        }
        
        val headers = mutableListOf("Evento")
        sortedPlayers.forEach { player ->
            if (isPdf) {
                val parts = player.split(" ").take(2)
                if (parts.size == 2) {
                    headers.add("${formatText(parts[0])}\n${formatText(parts[1])}")
                } else {
                    headers.add(formatText(parts.firstOrNull() ?: ""))
                }
            } else {
                headers.add(player)
            }
        }
        
        val rows = mutableListOf<List<String>>()
        if (events.isEmpty()) {
             val emptyRow = mutableListOf("Sin eventos")
             sortedPlayers.forEach { emptyRow.add("-") }
             rows.add(emptyRow)
        } else {
            for (event in events) {
                val dateStr = dateFormatter.formatTimestamp(event.date, if (isPdf) "dd/MM/yy" else "dd/MM/yyyy HH:mm")
                val eventRow = mutableListOf<String>()
                if (isPdf) {
                    val eventName = formatText(event.type.name)
                    eventRow.add("$eventName\n($dateStr)")
                } else {
                    eventRow.add("${event.type.name} ($dateStr)")
                }
                
                val eventAttendances = attendancesByEvent[event.id] ?: emptyList()
                for (player in sortedPlayers) {
                    val playerAttendance = eventAttendances.find { it.userName == player }
                    val statusText = playerAttendance?.status?.name ?: "-"
                    eventRow.add(formatText(statusText))
                }
                rows.add(eventRow)
            }
        }
        
        val eventTable = ReportTable(
            title = "Informe de Eventos",
            startOnNewPage = false,
            headers = headers,
            rows = rows
        )
        
        val title = "Informe de Eventos"
        
        val filePath = if (isPdf) {
            val tables = mutableListOf(eventTable)
            
            val metricsHeaders = mutableListOf("Métrica")
            metricsHeaders.addAll(headers.drop(1)) // Re-use player headers, which are already <= 8 chars
            
            fun calculateMetrics(statuses: List<AttendanceStatus>): Map<String, String> {
                val a = statuses.count { it == AttendanceStatus.ASISTENCIA }
                val r = statuses.count { it == AttendanceStatus.RETRASO }
                val j = statuses.count { it == AttendanceStatus.JUSTIFICADA }
                val na = statuses.count { it == AttendanceStatus.NO_ASISTENCIA }
                
                val convocatorias = a + r + j + na
                val faltas = j + na
                val asistenciasEfectivas = a + r
                
                val resFaltas = if (convocatorias == 0) "SIN\nASIST." 
                    else "${faltas}/${convocatorias}\n(${(faltas*100)/convocatorias}%)"
                    
                val resJustificadas = if (faltas == 0) "SIN\nFALTAS"
                    else "${j}/${faltas}\n(${(j*100)/faltas}%)"
                    
                val resRetrasos = if (asistenciasEfectivas == 0) "SIN\nASIST."
                    else "${r}/${asistenciasEfectivas}\n(${(r*100)/asistenciasEfectivas}%)"
                    
                return mapOf(
                    "Faltas" to resFaltas,
                    "Justifi." to resJustificadas,
                    "Retrasos" to resRetrasos
                )
            }

            val sections = listOf(
                "Entrenamientos" to EventType.ENTRENAMIENTO,
                "Partidos" to EventType.PARTIDO,
                "Totales" to null
            )
            
            var isFirstSection = true
            
            for ((sectionName, sectionType) in sections) {
                // For each section, we want a section header and then the metrics with the player headers inside
                val sectionRows = mutableListOf<List<String>>()
                
                val faltasRow = mutableListOf("[B] Faltas")
                val justificadasRow = mutableListOf("[B] Justifi.")
                val retrasosRow = mutableListOf("[B] Retrasos")
                
                for (player in sortedPlayers) {
                    val playerStatuses = events
                        .filter { sectionType == null || it.type == sectionType }
                        .mapNotNull { event ->
                            val att = attendancesByEvent[event.id]?.find { it.userName == player }
                            att?.status
                        }
                    
                    val metrics = calculateMetrics(playerStatuses)
                    
                    faltasRow.add(metrics["Faltas"] ?: "")
                    justificadasRow.add(metrics["Justifi."] ?: "")
                    retrasosRow.add(metrics["Retrasos"] ?: "")
                }
                
                sectionRows.add(faltasRow)
                sectionRows.add(justificadasRow)
                sectionRows.add(retrasosRow)
                
                val sectionTable = ReportTable(
                    title = if (isFirstSection) "Métricas\n\n$sectionName" else sectionName,
                    startOnNewPage = isFirstSection,
                    headers = metricsHeaders,
                    rows = sectionRows
                )
                
                tables.add(sectionTable)
                isFirstSection = false
            }
            
            exporter.exportPdf(title, tables)
        } else {
            val csvBuilder = StringBuilder()
            csvBuilder.append(headers.joinToString(",")).append("\n")
            rows.forEach { row ->
                val safeRow = row.map { it.replace(",", " ") }
                csvBuilder.append(safeRow.joinToString(",")).append("\n")
            }
            exporter.exportCsv(title, csvBuilder.toString())
        }
        
        val mimeType = if (isPdf) "application/pdf" else "text/csv"
        fileSharer.shareFile(filePath, mimeType)
    }
}

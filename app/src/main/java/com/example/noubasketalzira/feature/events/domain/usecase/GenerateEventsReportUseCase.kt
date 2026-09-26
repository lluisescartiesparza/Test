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
            return if (text.length > limit) text.substring(0, limit - 1) + "." else text
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
            
            // Build Metrics Table
            val metricsHeaders = mutableListOf("Métrica")
            metricsHeaders.addAll(headers.drop(1)) // Re-use player headers
            
            val metricsRows = mutableListOf<List<String>>()
            
            fun calculateMetrics(statuses: List<AttendanceStatus>): Map<String, String> {
                val a = statuses.count { it == AttendanceStatus.ASISTENCIA }
                val r = statuses.count { it == AttendanceStatus.RETRASO }
                val j = statuses.count { it == AttendanceStatus.JUSTIFICADA }
                val na = statuses.count { it == AttendanceStatus.NO_ASISTENCIA }
                
                val convocatorias = a + r + j + na
                val faltas = j + na
                val asistenciasEfectivas = a + r
                
                val resFaltas = if (convocatorias == 0) "SIN ASISTENCIA" 
                    else "${faltas}/${convocatorias} (${(faltas*100)/convocatorias}%)"
                    
                val resJustificadas = if (faltas == 0) "SIN FALTAS"
                    else "${j}/${faltas} (${(j*100)/faltas}%)"
                    
                val resRetrasos = if (asistenciasEfectivas == 0) "SIN ASISTENCIA"
                    else "${r}/${asistenciasEfectivas} (${(r*100)/asistenciasEfectivas}%)"
                    
                return mapOf(
                    "Faltas" to resFaltas,
                    "Justificadas" to resJustificadas,
                    "Retrasos" to resRetrasos
                )
            }

            val sections = listOf(
                "Entrenamientos" to EventType.ENTRENAMIENTO,
                "Partidos" to EventType.PARTIDO,
                "Totales" to null
            )
            
            for ((sectionName, sectionType) in sections) {
                // Section Header
                metricsRows.add(listOf("[SECTION] $sectionName"))
                
                val faltasRow = mutableListOf("[B] Faltas")
                val justificadasRow = mutableListOf("[B] Justificadas")
                val retrasosRow = mutableListOf("[B] Retrasos")
                
                for (player in sortedPlayers) {
                    val playerStatuses = events
                        .filter { sectionType == null || it.type == sectionType }
                        .mapNotNull { event ->
                            val att = attendancesByEvent[event.id]?.find { it.userName == player }
                            att?.status
                        }
                    
                    val metrics = calculateMetrics(playerStatuses)
                    
                    // We must abbreviate these too if they are long?
                    // But metrics are like "0/1 (0%)" which is 8-10 chars. Let's just output them directly.
                    // Oh, wait, "SIN ASISTENCIA" is 14 chars. 
                    // Let's use formatText for "SIN ASISTENCIA" so it fits in the column width, or just let it wrap natively?
                    // `drawMultilineText` in AndroidReportExporter doesn't wrap natively! It only wraps on explicit '\n'.
                    // So "SIN ASISTENCIA" might overflow the column. Let's replace "SIN ASISTENCIA" with "SIN\nASISTEN."
                    // Actually, let's just make it "SIN ASIST." or use `formatText` logic.
                    // Wait, the user said "mantener los límites de carácteres por columnas".
                    
                    fun formatMetric(text: String): String {
                        if (text == "SIN ASISTENCIA") return "SIN\nASIST."
                        if (text == "SIN FALTAS") return "SIN\nFALTAS"
                        return text
                    }
                    
                    faltasRow.add(formatMetric(metrics["Faltas"] ?: ""))
                    justificadasRow.add(formatMetric(metrics["Justificadas"] ?: ""))
                    retrasosRow.add(formatMetric(metrics["Retrasos"] ?: ""))
                }
                
                metricsRows.add(faltasRow)
                metricsRows.add(justificadasRow)
                metricsRows.add(retrasosRow)
            }
            
            val metricsTable = ReportTable(
                title = "Métricas",
                startOnNewPage = true,
                headers = metricsHeaders,
                rows = metricsRows
            )
            
            tables.add(metricsTable)
            
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

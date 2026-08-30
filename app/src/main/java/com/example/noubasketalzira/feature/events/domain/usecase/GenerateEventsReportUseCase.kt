package com.example.noubasketalzira.feature.events.domain.usecase

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import com.example.noubasketalzira.core.domain.util.IFileSharer
import com.example.noubasketalzira.core.domain.util.IReportExporter
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.flow.first

import com.example.noubasketalzira.feature.events.domain.model.EventType

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
        // 1. Obtener la foto actual de la base de datos local
        var events = repository.observeEvents(teamId).first()
        
        // Aplicar filtros
        if (eventType != null) {
            events = events.filter { it.type == eventType }
        }
        if (fromDateMillis != null) {
            events = events.filter { it.date >= fromDateMillis }
        }
        if (toDateMillis != null) {
            events = events.filter { it.date <= toDateMillis }
        }
        
        // Ordenar más viejos arriba (ascendente por fecha)
        events = events.sortedBy { it.date }
        
        // 2. Preparar los datos
        // Extraemos las asistencias de cada evento
        val attendancesByEvent = mutableMapOf<String, List<com.example.noubasketalzira.feature.events.domain.model.Attendance>>()
        val allPlayerNames = mutableSetOf<String>()
        
        for (event in events) {
            val attendances = repository.observeAttendance(event.id).first()
            attendancesByEvent[event.id] = attendances
            attendances.forEach { allPlayerNames.add(it.userName) }
        }
        
        // Ordenamos alfabéticamente a los jugadores
        val sortedPlayers = allPlayerNames.sorted()
        
        val isPdf = format.lowercase() == "pdf"
        
        // Función para abreviar texto largo solo en PDF
        fun formatText(text: String, limit: Int = 8): String {
            if (!isPdf) return text
            return if (text.length > limit) text.substring(0, limit - 1) + "." else text
        }
        
        // Montamos las cabeceras: "Evento" + cada jugador
        val headers = mutableListOf("Evento")
        sortedPlayers.forEach { player ->
            if (isPdf) {
                // Cogemos como máximo las 2 primeras palabras y las apilamos en PDF
                val parts = player.split(" ").take(2)
                if (parts.size == 2) {
                    headers.add("${formatText(parts[0])}\n${formatText(parts[1])}")
                } else {
                    headers.add(formatText(parts.firstOrNull() ?: ""))
                }
            } else {
                // En CSV, nombre completo sin saltos de línea
                headers.add(player)
            }
        }
        
        // Montamos las filas (una por evento)
        val rows = mutableListOf<List<String>>()
        if (events.isEmpty()) {
             val emptyRow = mutableListOf("Sin eventos")
             sortedPlayers.forEach { emptyRow.add("-") }
             rows.add(emptyRow)
        } else {
            for (event in events) {
                // Columna 1: Evento
                val dateStr = dateFormatter.formatTimestamp(event.date, if (isPdf) "dd/MM/yy" else "dd/MM/yyyy HH:mm")
                
                val eventRow = mutableListOf<String>()
                if (isPdf) {
                    val eventName = formatText(event.type.name)
                    eventRow.add("$eventName\n($dateStr)")
                } else {
                    eventRow.add("${event.type.name} ($dateStr)")
                }
                
                // Columnas de jugadores
                val eventAttendances = attendancesByEvent[event.id] ?: emptyList()
                for (player in sortedPlayers) {
                    val playerAttendance = eventAttendances.find { it.userName == player }
                    val statusText = playerAttendance?.status?.name ?: "-"
                    eventRow.add(formatText(statusText))
                }
                rows.add(eventRow)
            }
        }
        
        val title = "Informe de Eventos"
        
        // 3. Generar archivo según formato
        val filePath = if (format.lowercase() == "pdf") {
            exporter.exportPdf(title, headers, rows)
        } else {
            val csvBuilder = StringBuilder()
            csvBuilder.append(headers.joinToString(",")).append("\n")
            rows.forEach { row ->
                // Clean commas to avoid breaking CSV format
                val safeRow = row.map { it.replace(",", " ") }
                csvBuilder.append(safeRow.joinToString(",")).append("\n")
            }
            exporter.exportCsv(title, csvBuilder.toString())
        }
        
        // 4. Compartir archivo
        val mimeType = if (format.lowercase() == "pdf") "application/pdf" else "text/csv"
        fileSharer.shareFile(filePath, mimeType)
    }
}

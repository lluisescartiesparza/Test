package com.example.noubasketalzira.feature.events.domain.usecase

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import com.example.noubasketalzira.core.domain.util.IFileSharer
import com.example.noubasketalzira.core.domain.util.IReportExporter
import com.example.noubasketalzira.feature.events.domain.repository.IEventRepository
import kotlinx.coroutines.flow.first

class GenerateEventsReportUseCase(
    private val repository: IEventRepository,
    private val exporter: IReportExporter,
    private val fileSharer: IFileSharer,
    private val dateFormatter: IDateFormatter
) {
    suspend operator fun invoke(teamId: String, format: String) {
        // 1. Obtener la foto actual de la base de datos local
        val events = repository.observeEvents(teamId).first()
        
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
        
        // Montamos las cabeceras: "Jugador" + cada evento en formato TIPO (dd/MM/yy)
        val headers = mutableListOf("Jugador")
        events.forEach { event ->
            // Usamos formato yy (año a 2 dígitos) según el ejemplo del usuario (01/10/26)
            val dateStr = dateFormatter.formatTimestamp(event.date, "dd/MM/yy")
            headers.add("${event.type.name} ($dateStr)")
        }
        
        // Montamos las filas (una por jugador)
        val rows = mutableListOf<List<String>>()
        if (sortedPlayers.isEmpty()) {
             // Si no hay jugadores, añadir una fila vacía para que conste algo
             val emptyRow = mutableListOf("Sin jugadores")
             events.forEach { emptyRow.add("-") }
             rows.add(emptyRow)
        } else {
            for (player in sortedPlayers) {
                val playerRow = mutableListOf(player)
                for (event in events) {
                    val eventAttendances = attendancesByEvent[event.id] ?: emptyList()
                    val playerAttendance = eventAttendances.find { it.userName == player }
                    playerRow.add(playerAttendance?.status?.name ?: "-")
                }
                rows.add(playerRow)
            }
        }
        
        val title = "Informe de Eventos"
        
        // 3. Generar archivo según formato
        val filePath = if (format.lowercase() == "pdf") {
            exporter.exportPdf(title, headers, rows)
        } else {
            val csvBuilder = java.lang.StringBuilder()
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

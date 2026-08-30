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
        val headers = listOf("Fecha", "Tipo", "Descripción", "Jugador", "Estado")
        val rows = mutableListOf<List<String>>()
        
        for (event in events) {
            val attendances = repository.observeAttendance(event.id).first()
            val dateStr = dateFormatter.formatTimestamp(event.date, "dd/MM/yyyy HH:mm")
            
            if (attendances.isEmpty()) {
                // Si no hay jugadores, añadir una fila vacía para que conste el evento
                rows.add(listOf(
                    dateStr,
                    event.type.name,
                    event.description ?: "-",
                    "Sin jugadores",
                    "-"
                ))
            } else {
                // Desglose por jugador
                for (attendance in attendances) {
                    rows.add(listOf(
                        dateStr,
                        event.type.name,
                        event.description ?: "-",
                        attendance.userName,
                        attendance.status.name
                    ))
                }
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

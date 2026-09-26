package com.example.noubasketalzira.core.domain.util

data class ReportTable(
    val title: String,
    val startOnNewPage: Boolean,
    val headers: List<String>,
    val rows: List<List<String>>
)

interface IReportExporter {
    suspend fun exportPdf(documentTitle: String, tables: List<ReportTable>): String
    suspend fun exportCsv(title: String, csvContent: String): String
}

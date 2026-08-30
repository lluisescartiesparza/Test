package com.example.noubasketalzira.core.domain.util

/**
 * Interfaz genérica para exportar contenido a archivos (PDF, CSV, etc.)
 * Se mantiene en la capa Domain (KMP Ready) sin depender de clases de Android.
 */
interface IReportExporter {
    /**
     * Exporta el contenido tabular a PDF.
     * @param title El título del documento.
     * @param headers Las cabeceras de la tabla.
     * @param rows Las filas de la tabla.
     * @return La ruta absoluta o URI del archivo generado.
     */
    suspend fun exportPdf(title: String, headers: List<String>, rows: List<List<String>>): String

    /**
     * Exporta el contenido a CSV.
     * @param title El prefijo para el nombre del archivo.
     * @param csvContent El contenido crudo en formato CSV.
     * @return La ruta absoluta o URI del archivo generado.
     */
    suspend fun exportCsv(title: String, csvContent: String): String
}

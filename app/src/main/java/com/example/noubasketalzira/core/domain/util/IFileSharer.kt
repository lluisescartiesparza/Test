package com.example.noubasketalzira.core.domain.util

/**
 * Interfaz genérica para compartir archivos a través del OS.
 * KMP Ready: La implementación específica lidiará con Intents de Android o APIs de iOS.
 */
interface IFileSharer {
    /**
     * Comparte un archivo a través del menú nativo del sistema operativo.
     * @param filePath La ruta absoluta o URI generada por IReportExporter.
     * @param mimeType El tipo MIME del archivo (ej. "application/pdf" o "text/csv").
     */
    fun shareFile(filePath: String, mimeType: String)
}

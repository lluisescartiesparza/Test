package com.example.noubasketalzira.core.framework.android.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.noubasketalzira.core.domain.util.IReportExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidReportExporter(
    private val context: Context
) : IReportExporter {

    override suspend fun exportPdf(title: String, headers: List<String>, rows: List<List<String>>): String {
        return withContext(Dispatchers.IO) {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape at 72 PPI
            
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            
            val paint = Paint()
            paint.color = Color.BLACK
            
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }
            
            val colsCount = headers.size.coerceAtLeast(1)
            val dynamicTextSize = if (colsCount > 5) 8f else 10f
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = dynamicTextSize + 1f
                isFakeBoldText = true
            }
            
            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = dynamicTextSize
            }

            var currentY = 50f
            val marginX = 50f
            val columnWidths = calculateColumnWidths(headers, rows, 742f) // 842 - 100 margin
            
            // Draw title
            canvas.drawText(title, marginX, currentY, titlePaint)
            currentY += 40f
            
            // Draw Headers
            var currentX = marginX
            headers.forEachIndexed { index, header ->
                canvas.drawText(header, currentX, currentY, headerPaint)
                currentX += columnWidths.getOrElse(index) { 100f }
            }
            currentY += 20f
            
            // Draw rows
            rows.forEach { row ->
                // Check if we need a new page
                if (currentY > 550f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 50f
                }
                
                currentX = marginX
                row.forEachIndexed { index, cell ->
                    // Simple word wrap or truncation is usually needed, but for simplicity we just draw
                    val textToDraw = if (cell.length > 30) cell.take(27) + "..." else cell
                    canvas.drawText(textToDraw, currentX, currentY, textPaint)
                    currentX += columnWidths.getOrElse(index) { 100f }
                }
                currentY += 20f
            }
            
            document.finishPage(page)
            
            // Save file
            val file = File(context.cacheDir, "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            
            file.absolutePath
        }
    }

    override suspend fun exportCsv(title: String, csvContent: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "${title.replace(" ", "_")}_${System.currentTimeMillis()}.csv")
            file.writeText(csvContent)
            file.absolutePath
        }
    }
    
    private fun calculateColumnWidths(headers: List<String>, rows: List<List<String>>, availableWidth: Float): List<Float> {
        val colsCount = headers.size
        if (colsCount == 0) return emptyList()
        val defaultWidth = availableWidth / colsCount
        return List(colsCount) { defaultWidth }
    }
}

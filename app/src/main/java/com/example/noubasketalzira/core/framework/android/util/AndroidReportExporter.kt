package com.example.noubasketalzira.core.framework.android.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.noubasketalzira.core.domain.util.IReportExporter
import com.example.noubasketalzira.core.domain.util.ReportTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidReportExporter(
    private val context: Context
) : IReportExporter {

    override suspend fun exportPdf(documentTitle: String, tables: List<ReportTable>): String {
        return withContext(Dispatchers.IO) {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
            
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            fun drawMultilineText(text: String, x: Float, y: Float, textPaint: Paint): Float {
                val lines = text.split("\n")
                var cy = y
                for (line in lines) {
                    canvas.drawText(line, x, cy, textPaint)
                    cy += textPaint.textSize + 2f
                }
                return (lines.size - 1) * (textPaint.textSize + 2f)
            }

            var currentY = 50f
            val marginX = 50f
            
            for (table in tables) {
                if (table.startOnNewPage && currentY > 50f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 50f
                }
                
                // Draw Title
                if (table.title.isNotBlank()) {
                    canvas.drawText(table.title, marginX, currentY, titlePaint)
                    currentY += 40f
                }

                val colsCount = table.headers.size.coerceAtLeast(1)
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
                val sectionPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = dynamicTextSize + 1f
                    isFakeBoldText = true
                }
                
                val columnWidths = calculateColumnWidths(table.headers, table.rows, 742f)
                
                // Draw Headers
                var currentX = marginX
                var maxHeaderHeight = 0f
                table.headers.forEachIndexed { index, header ->
                    val addedHeight = drawMultilineText(header, currentX, currentY, headerPaint)
                    if (addedHeight > maxHeaderHeight) maxHeaderHeight = addedHeight
                    currentX += columnWidths.getOrElse(index) { 100f }
                }
                currentY += maxHeaderHeight + 20f
                
                // Draw Rows
                table.rows.forEach { row ->
                    // Check if we need a new page
                    if (currentY > 550f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 50f
                    }
                    
                    currentX = marginX
                    var maxRowHeight = 0f
                    
                    // Special case for section headers (row with only 1 element that we want to span, or we just draw it bold)
                    val isSection = row.size == 1 && row[0].startsWith("[SECTION]")
                    
                    if (isSection) {
                        val sectionText = row[0].replace("[SECTION]", "").trim()
                        val addedHeight = drawMultilineText(sectionText, currentX, currentY, sectionPaint)
                        maxRowHeight = addedHeight
                    } else {
                        row.forEachIndexed { index, cell ->
                            // if cell starts with [B], draw bold
                            val isBold = cell.startsWith("[B]")
                            val cleanCell = cell.replace("[B]", "").trim()
                            val paintToUse = if (isBold) sectionPaint else textPaint
                            
                            val addedHeight = drawMultilineText(cleanCell, currentX, currentY, paintToUse)
                            if (addedHeight > maxRowHeight) maxRowHeight = addedHeight
                            currentX += columnWidths.getOrElse(index) { 100f }
                        }
                    }
                    currentY += maxRowHeight + 20f
                }
                
                currentY += 20f // Margin bottom after table
            }
            
            document.finishPage(page)
            
            // Save file
            val file = File(context.cacheDir, "${documentTitle.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
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
        
        // As a simple heuristic for the metrics table, if first column is "Métrica", we give it a bit more space.
        // But evenly distributed is fine for now.
        val defaultWidth = availableWidth / colsCount
        return List(colsCount) { defaultWidth }
    }
}

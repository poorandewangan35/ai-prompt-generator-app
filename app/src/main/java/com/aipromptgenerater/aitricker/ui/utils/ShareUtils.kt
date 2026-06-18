package com.aipromptgenerater.aitricker.ui.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.text.TextPaint
import android.text.StaticLayout
import android.text.Layout
import java.io.OutputStream

fun copyToClipboard(context: Context, text: String, label: String = "AI Generated Prompt") {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("CLIPBOARD_COPY", "Failed to copy text", e)
        Toast.makeText(context, "Failed to copy text", Toast.LENGTH_SHORT).show()
    }
}

fun shareToWhatsApp(context: Context, text: String) {
    try {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        context.startActivity(whatsappIntent)
    } catch (e: Exception) {
        Log.e("SHARE_WHATSAPP", "Failed to share directly via WhatsApp, attempting fallback share chooser", e)
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Prompt"))
        } catch (innerEx: Exception) {
            Log.e("SHARE_WHATSAPP", "Fallback share chooser failed", innerEx)
            Toast.makeText(context, "Sharing failed: ${innerEx.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun savePromptAsPdf(context: Context, title: String, text: String) {
    try {
        val pdfDocument = PdfDocument()
        
        // Page dimensions: A4 is 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        
        val margin = 40
        val contentWidth = pageWidth - (margin * 2)
        
        // Setup text paint
        val textPaint = TextPaint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        
        val titlePaint = TextPaint().apply {
            textSize = 16f
            color = android.graphics.Color.BLACK
            isFakeBoldText = true
        }

        // We use StaticLayout to handle multi-line text wrapping automatically
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.1f)
            .setIncludePad(true)
            .build()
            
        // Calculate page count and draw
        val totalLines = staticLayout.lineCount
        
        var pageNumber = 1
        var currentY = margin + 40 // starting vertical position
        
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        // Draw title on first page
        canvas.drawText(title, margin.toFloat(), margin.toFloat() + 10, titlePaint)
        canvas.drawLine(margin.toFloat(), margin.toFloat() + 20, (pageWidth - margin).toFloat(), margin.toFloat() + 20, Paint().apply { strokeWidth = 1f; color = android.graphics.Color.GRAY })
        
        for (line in 0 until totalLines) {
            val lineTop = staticLayout.getLineTop(line)
            val lineBottom = staticLayout.getLineBottom(line)
            val lineBaseline = staticLayout.getLineBaseline(line)
            val lineHeight = lineBottom - lineTop
            
            // Check if line exceeds page height
            if (currentY + lineHeight > pageHeight - margin) {
                // Draw page number for current page
                val pageNumText = "Page $pageNumber"
                canvas.drawText(pageNumText, (pageWidth / 2 - textPaint.measureText(pageNumText) / 2), (pageHeight - margin / 2).toFloat(), textPaint)
                
                // Finish current page
                pdfDocument.finishPage(page)
                
                // Start a new page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }
            
            val lineStart = staticLayout.getLineStart(line)
            val lineEnd = staticLayout.getLineEnd(line)
            val lineText = text.subSequence(lineStart, lineEnd).toString().replace("\r", "").replace("\n", "")
            
            canvas.drawText(lineText, margin.toFloat(), currentY.toFloat() + (lineBaseline - lineTop), textPaint)
            currentY += lineHeight
        }
        
        // Draw page number for last page
        val pageNumText = "Page $pageNumber"
        canvas.drawText(pageNumText, (pageWidth / 2 - textPaint.measureText(pageNumText) / 2), (pageHeight - margin / 2).toFloat(), textPaint)
        
        pdfDocument.finishPage(page)
        
        // Save to MediaStore (Downloads folder)
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to open output stream", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to create PDF file in MediaStore", Toast.LENGTH_SHORT).show()
        }
        
        pdfDocument.close()
    } catch (e: Exception) {
        Log.e("PDF_GEN", "Error creating PDF", e)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

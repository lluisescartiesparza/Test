package com.example.noubasketalzira.core.framework.android.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.noubasketalzira.core.domain.util.IFileSharer
import java.io.File

class AndroidFileSharer(
    private val context: Context
) : IFileSharer {

    override fun shareFile(filePath: String, mimeType: String) {
        val file = File(filePath)
        if (!file.exists()) return

        // Usamos el authority definido en el AndroidManifest.xml (usualmente applicationID + .fileprovider)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Lanzamos el intent usando el contexto de la aplicación, necesitamos FLAG_ACTIVITY_NEW_TASK
        val chooser = Intent.createChooser(shareIntent, "Compartir informe")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

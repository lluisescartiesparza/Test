package com.example.noubasketalzira.core.framework.android.util

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidDateFormatter : IDateFormatter {
    override fun formatToIso8601(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date(timestamp))
    }

    override fun parseIso8601ToTimestamp(dateString: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

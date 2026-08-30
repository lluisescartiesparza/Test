package com.example.noubasketalzira.core.framework.android.util

import com.example.noubasketalzira.core.domain.util.IDateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidDateFormatter : IDateFormatter {
    override fun formatTimestamp(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}

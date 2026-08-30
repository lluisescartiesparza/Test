package com.example.noubasketalzira.core.domain.util

interface IDateFormatter {
    fun formatTimestamp(timestamp: Long, pattern: String): String
    fun parseIso8601ToTimestamp(isoString: String): Long
}

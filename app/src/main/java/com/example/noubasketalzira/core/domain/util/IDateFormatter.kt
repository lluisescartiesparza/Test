package com.example.noubasketalzira.core.domain.util

interface IDateFormatter {
    fun formatToIso8601(timestamp: Long): String
    fun parseIso8601ToTimestamp(dateString: String): Long
}

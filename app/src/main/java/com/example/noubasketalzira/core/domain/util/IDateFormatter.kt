package com.example.noubasketalzira.core.domain.util

interface IDateFormatter {
    fun formatTimestamp(timestamp: Long, pattern: String): String
}

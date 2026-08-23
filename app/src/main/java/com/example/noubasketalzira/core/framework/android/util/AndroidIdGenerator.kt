package com.example.noubasketalzira.core.framework.android.util

import com.example.noubasketalzira.core.domain.util.IIdGenerator
import java.util.UUID

class AndroidIdGenerator : IIdGenerator {
    override fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}

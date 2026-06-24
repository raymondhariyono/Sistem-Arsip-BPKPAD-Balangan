package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.ActivityLog
import com.example.arsipbpkpad.domain.model.DomainResult

interface ActivityLogRepository {
    suspend fun logActivity(log: ActivityLog): DomainResult<Unit>
}

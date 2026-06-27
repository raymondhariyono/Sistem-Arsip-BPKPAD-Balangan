package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ActivityLog
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for fetching activity logs for a specific entity.
 */
class GetActivityLogsForEntityUseCase @Inject constructor(
    private val activityLogRepository: ActivityLogRepository
) {
    operator fun invoke(entityId: String, entityType: String = "archive_documents"): Flow<DomainResult<List<ActivityLog>>> {
        return activityLogRepository.getActivityLogsForEntity(entityId, entityType)
    }
}

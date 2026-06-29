package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.TransactionBundleDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.AuthRepository
import com.example.arsipbpkpad.domain.repository.TransactionBundleRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of TransactionBundleRepository using Supabase.
 */
class TransactionBundleRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : TransactionBundleRepository {

    override suspend fun createBundle(
        name: String,
        description: String?,
        year: Int
    ): DomainResult<String> {
        return safeApiCall(ioDispatcher) {
            val bundle = TransactionBundleDto(
                bundleName = name,
                description = description,
                year = year,
                createdBy = authRepository.getCurrentUserId()
            )

            val inserted = supabaseClient.postgrest["transaction_bundles"]
                .insert(bundle) {
                    select()
                }
                .decodeSingle<TransactionBundleDto>()

            inserted.id!!
        }
    }

    override suspend fun softDeleteBundle(bundleId: String): DomainResult<Unit> {
        return safeApiCall(ioDispatcher) {
            val now = java.time.OffsetDateTime.now().toString()
            supabaseClient.postgrest["transaction_bundles"].update({
                "deleted_at" to now
            }) {
                filter { eq("id", bundleId) }
            }
            Unit
        }
    }
}

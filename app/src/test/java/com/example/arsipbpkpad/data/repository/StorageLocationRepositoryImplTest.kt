package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.BoxDto
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ActivityLogRepository
import com.example.arsipbpkpad.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StorageLocationRepositoryImplTest {

    private val supabaseClient = mockk<SupabaseClient>()
    private val postgrest = mockk<Postgrest>()
    private val activityLogRepository = mockk<ActivityLogRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val ioDispatcher = Dispatchers.Unconfined
    
    private lateinit var repository: StorageLocationRepositoryImpl

    @Before
    fun setup() {
        mockkStatic("io.github.jan.supabase.postgrest.PostgrestKt")
        every { supabaseClient.postgrest } returns postgrest
        
        repository = spyk(StorageLocationRepositoryImpl(
            supabaseClient,
            activityLogRepository,
            authRepository,
            ioDispatcher
        ))
    }

    @Test
    fun `LOC_001 - getOrCreateLocation returns existing box id`() = runTest {
        val room = "Gedung A"
        val shelf = "Rak 1"
        val box = "Box 1"
        
        coEvery { repository.getRoomByName(room) } returns com.example.arsipbpkpad.domain.model.Room("R1", room)
        coEvery { repository.getShelfByName("R1", shelf) } returns com.example.arsipbpkpad.domain.model.Shelf("S1", "R1", shelf)
        
        val boxQueryBuilder = mockk<PostgrestQueryBuilder>()
        every { postgrest["boxes"] } returns boxQueryBuilder
        
        val mockResult = mockk<PostgrestResult>(relaxed = true)
        coEvery { boxQueryBuilder.select(any(), any()) } returns mockResult
        every { mockResult.decodeSingleOrNull<BoxDto>() } returns BoxDto(id = "B1-UUID", shelfId = "S1", name = box)
        
        val result = repository.getOrCreateLocation(room, shelf, box, "2026")
        
        assertTrue("Expected Success but was $result", result is DomainResult.Success)
        assertEquals("B1-UUID", (result as DomainResult.Success).data)
    }

    @Test
    fun `LOC_004 - getOrCreateLocation creates box if not exists`() = runTest {
        val room = "Gedung A"
        val shelf = "Rak 1"
        val box = "Box 1"
        
        coEvery { repository.getRoomByName(room) } returns com.example.arsipbpkpad.domain.model.Room("R1", room)
        coEvery { repository.getShelfByName("R1", shelf) } returns com.example.arsipbpkpad.domain.model.Shelf("S1", "R1", shelf)
        
        val boxQueryBuilder = mockk<PostgrestQueryBuilder>()
        every { postgrest["boxes"] } returns boxQueryBuilder
        
        val mockResult = mockk<PostgrestResult>(relaxed = true)
        coEvery { boxQueryBuilder.select(any(), any()) } returns mockResult
        every { mockResult.decodeSingleOrNull<BoxDto>() } returns null
        
        coEvery { repository.createBox("S1", box) } returns Result.success(com.example.arsipbpkpad.domain.model.Box("NEW-B1", "S1", box))
        
        val result = repository.getOrCreateLocation(room, shelf, box, "2026")
        
        assertTrue(result is DomainResult.Success)
        assertEquals("NEW-B1", (result as DomainResult.Success).data)
    }
}

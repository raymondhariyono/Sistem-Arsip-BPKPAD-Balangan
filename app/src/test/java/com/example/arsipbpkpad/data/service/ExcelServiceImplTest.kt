package com.example.arsipbpkpad.data.service

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExcelServiceImplTest {

    private lateinit var excelService: ExcelServiceImpl

    @Before
    fun setup() {
        excelService = ExcelServiceImpl(Dispatchers.Unconfined)
    }

    @Test
    fun `EXP_008 - exportToExcel produces a valid output stream`() = runTest {
        val archives = listOf(
            createArchive("1", "SP2D-001"),
            createArchive("2", "SPM-002")
        )
        val outputStream = ByteArrayOutputStream()

        excelService.exportToExcel(archives, outputStream)

        val bytes = outputStream.toByteArray()
        assertTrue(bytes.isNotEmpty())
        // Further verification would require parsing the Excel file, 
        // which is possible with Apache POI in the test.
    }

    @Test
    fun `IMP_001 - importFromExcel parses valid excel file`() = runTest {
        val archives = listOf(
            createArchive("1", "SP2D-IMPORT")
        )
        val outputStream = ByteArrayOutputStream()
        excelService.exportToExcel(archives, outputStream)
        
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val result = excelService.importFromExcel(inputStream)

        assertEquals(1, result.size)
        assertEquals("SP2D-IMPORT", result[0].documentNumber)
    }

    private fun createArchive(id: String, docNumber: String): ArchiveDocument {
        return ArchiveDocument(
            id = id,
            type = DocType.SP2D,
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            documentNumber = docNumber,
            description = "Test",
            nominal = 1000.0,
            year = 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            metadata = null,
            idStorageLocation = "loc-1",
            bundleId = null
        )
    }
}

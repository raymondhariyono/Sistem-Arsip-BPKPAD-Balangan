package com.example.arsipbpkpad.data.service

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.DomainConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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

    @Test
    fun `IMP_002 - importFromExcel skips empty rows and header rows`() = runTest {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Arsip")
        
        // Header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("No.")
        headerRow.createCell(1).setCellValue("Kode Klasifikasi")
        
        // Empty row
        sheet.createRow(1)
        
        // Duplicate header row
        val subHeaderRow = sheet.createRow(2)
        subHeaderRow.createCell(0).setCellValue("No.")
        
        // Data row
        val dataRow = sheet.createRow(3)
        dataRow.createCell(1).setCellValue("900.1.3.1")
        dataRow.createCell(2).setCellValue("DOC-001")
        dataRow.createCell(3).setCellValue("Description")
        dataRow.createCell(4).setCellValue(2026.0)

        // Row with blank doc number and description (should be skipped)
        val blankRow = sheet.createRow(4)
        blankRow.createCell(1).setCellValue("900.1.3.1")
        blankRow.createCell(4).setCellValue(2026.0)

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val result = excelService.importFromExcel(inputStream)

        assertEquals(1, result.size)
        assertEquals("DOC-001", result[0].documentNumber)
    }

    @Test
    fun `IMP_003 - importFromExcel uses default classification code`() = runTest {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Arsip")
        
        // Data row with missing classification code
        val dataRow = sheet.createRow(1)
        dataRow.createCell(2).setCellValue("DOC-002")
        dataRow.createCell(3).setCellValue("Description")
        dataRow.createCell(4).setCellValue(2026.0)

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val result = excelService.importFromExcel(inputStream)

        assertEquals(1, result.size)
        assertEquals(DomainConstants.DEFAULT_CLASSIFICATION_CODE, result[0].classificationCode)
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
            metadata = ArchiveMetadata(warehouse = "G-1", rack = "R-1", boxNumber = "B-1"),
            idStorageLocation = "loc-1",
            bundleId = null
        )
    }
}

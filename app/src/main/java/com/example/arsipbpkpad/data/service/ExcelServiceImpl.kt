package com.example.arsipbpkpad.data.service

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.service.ExcelService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ExcelServiceImpl @Inject constructor(
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ExcelService {

    override suspend fun exportToExcel(archives: List<ArchiveDocument>, outputStream: OutputStream) {
        withContext(ioDispatcher) {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Arsip")

            // Create Header
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "No.", "Kode Klasifikasi", "Nomor Isi Berkas", "URAIAN", "TAHUN",
                "Tingkat Perkembangan", "Media", "Kondisi", "Jumlah", "Ruang", "Rak", "Tingkat", "Box"
            )
            headers.forEachIndexed { index, title ->
                headerRow.createCell(index).setCellValue(title)
            }

            // Fill Data
            archives.forEachIndexed { index, archive ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue((index + 1).toDouble())
                row.createCell(1).setCellValue(archive.classificationCode)
                row.createCell(2).setCellValue(archive.documentNumber ?: "")
                row.createCell(3).setCellValue(archive.description ?: "")
                row.createCell(4).setCellValue(archive.year.toDouble())
                row.createCell(5).setCellValue(if (archive.copyType == DocCopyType.ORIGINAL) "asli" else "kopi")
                row.createCell(6).setCellValue("kertas") // Media default
                row.createCell(7).setCellValue(when(archive.condition) {
                    DocCondition.GOOD -> "baik"
                    DocCondition.DAMAGED -> "rusak"
                    DocCondition.LOST -> "hilang"
                })
                row.createCell(8).setCellValue("${archive.copyCount} berkas")
                row.createCell(9).setCellValue(archive.metadata?.warehouse ?: "")
                row.createCell(10).setCellValue(archive.metadata?.rack ?: "")
                row.createCell(11).setCellValue("") // Tingkat ignored for now
                row.createCell(12).setCellValue(archive.metadata?.boxNumber ?: "")
            }

            workbook.write(outputStream)
            workbook.close()
        }
    }

    override suspend fun importFromExcel(inputStream: InputStream): List<ArchiveDocument> {
        return withContext(ioDispatcher) {
            val archives = mutableListOf<ArchiveDocument>()
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Start from row 2 (index 2) as seen in screenshots (Row 1 Header, Row 2 Sub-header/Labels)
            // Or row 3 (index 2) if row 1 is "No." and row 2 is "nomor sampul berkas"
            // Looking at the screenshots, data starts on row 3 (index 2).
            for (i in 2..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                
                val classificationCode = getCellStringValue(row.getCell(1)) ?: "000.1.2.1"
                val docNumber = getCellStringValue(row.getCell(2))
                val description = getCellStringValue(row.getCell(3))
                val year = getCellNumericValue(row.getCell(4))?.toInt() ?: 2024
                val copyTypeStr = getCellStringValue(row.getCell(5))?.lowercase()
                val copyType = if (copyTypeStr == "asli") DocCopyType.ORIGINAL else DocCopyType.COPY
                
                val conditionStr = getCellStringValue(row.getCell(7))?.lowercase()
                val condition = when(conditionStr) {
                    "baik" -> DocCondition.GOOD
                    "rusak" -> DocCondition.DAMAGED
                    "hilang" -> DocCondition.LOST
                    else -> DocCondition.GOOD
                }
                
                val countStr = getCellStringValue(row.getCell(8)) ?: "1"
                val count = countStr.split(" ").firstOrNull()?.toIntOrNull() ?: 1
                
                val warehouse = getCellStringValue(row.getCell(9))
                val rack = getCellStringValue(row.getCell(10))
                val box = getCellStringValue(row.getCell(12))

                archives.add(
                    ArchiveDocument(
                        id = UUID.randomUUID().toString(),
                        type = inferDocType(docNumber),
                        documentNumber = docNumber,
                        copyType = copyType,
                        copyCount = count,
                        classificationCode = classificationCode,
                        description = description,
                        nominal = null, // Nominal not in current Excel structure
                        year = year,
                        condition = condition,
                        status = DocStatus.AVAILABLE,
                        metadata = ArchiveMetadata(
                            warehouse = warehouse,
                            rack = rack,
                            boxNumber = box
                        )
                    )
                )
            }
            workbook.close()
            archives
        }
    }

    private fun getCellStringValue(cell: Cell?): String? {
        if (cell == null) return null
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> null
        }
    }

    private fun getCellNumericValue(cell: Cell?): Double? {
        if (cell == null) return null
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> cell.stringCellValue.toDoubleOrNull()
            else -> null
        }
    }

    private fun inferDocType(docNumber: String?): DocType {
        if (docNumber == null) return DocType.SP2D
        return when {
            docNumber.contains("SP2D", ignoreCase = true) -> DocType.SP2D
            docNumber.contains("SPM", ignoreCase = true) -> DocType.SPM
            docNumber.contains("SPP", ignoreCase = true) -> DocType.SPP
            docNumber.contains("SPJ", ignoreCase = true) -> DocType.SPJ
            else -> DocType.SP2D
        }
    }
}

package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.dao.YearStatEntity
import com.example.arsipbpkpad.domain.model.YearStats

fun YearStatEntity.toDomain(): YearStats {
    return YearStats(
        year = year,
        count = count,
        lastUpdated = lastUpdated ?: "-"
    )
}

package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.ClassificationCodeEntity
import com.example.arsipbpkpad.data.remote.dto.ClassificationCodeDto
import com.example.arsipbpkpad.domain.model.ClassificationCode

fun ClassificationCodeDto.toEntity(): ClassificationCodeEntity {
    return ClassificationCodeEntity(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level,
        isActive = isActive
    )
}

fun ClassificationCodeEntity.toDomain(): ClassificationCode {
    return ClassificationCode(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level,
        isActive = isActive
    )
}

fun ClassificationCode.toEntity(): ClassificationCodeEntity {
    return ClassificationCodeEntity(
        code = code,
        name = name,
        parentCode = parentCode,
        level = level,
        isActive = isActive
    )
}

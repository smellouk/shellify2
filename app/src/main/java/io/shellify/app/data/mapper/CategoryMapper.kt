package io.shellify.app.data.mapper

import io.shellify.app.data.local.entity.CategoryEntity
import io.shellify.app.domain.model.Category

fun CategoryEntity.toDomain(): Category =
    Category(id = id, name = name, sortIndex = sortIndex, icon = icon, color = color)

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(id = id, name = name, sortIndex = sortIndex, icon = icon, color = color)

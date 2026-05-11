package dev.pwaforge.data.mapper

import dev.pwaforge.data.local.entity.CategoryEntity
import dev.pwaforge.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(id = id, name = name, sortIndex = sortIndex, icon = icon, color = color)
fun Category.toEntity(): CategoryEntity = CategoryEntity(id = id, name = name, sortIndex = sortIndex, icon = icon, color = color)

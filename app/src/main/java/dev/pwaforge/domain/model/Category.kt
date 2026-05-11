package dev.pwaforge.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val sortIndex: Int = 0,
)

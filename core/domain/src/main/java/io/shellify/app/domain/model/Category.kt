package io.shellify.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val sortIndex: Int = 0,
    val icon: String = "folder",
    val color: String = "#6D28D9",
)

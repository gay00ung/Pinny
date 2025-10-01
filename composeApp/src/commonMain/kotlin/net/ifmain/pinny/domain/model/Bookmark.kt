package net.ifmain.pinny.domain.model

data class Bookmark(
    val id: String,
    val url: String,
    val title: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val category: String?,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean
)

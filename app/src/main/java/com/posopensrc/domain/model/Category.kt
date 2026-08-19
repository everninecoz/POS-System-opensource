package com.posopensrc.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val sortOrder: Int = 0
)

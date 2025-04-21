package ru.vcarstein.kokodi.dto

data class PlayerStatus(
    val id: Long,
    val name: String,
    val score: Int,
    val isBlocked: Boolean,
)

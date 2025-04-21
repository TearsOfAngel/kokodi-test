package ru.vcarstein.kokodi.exception

data class ErrorResponse(
    val message: String,
    val details: String? = null
)

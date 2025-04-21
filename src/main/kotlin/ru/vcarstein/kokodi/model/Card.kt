package ru.vcarstein.kokodi.model

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class Card(
    val name: String,
    @Enumerated(EnumType.STRING)
    val type: CardType,
    val value: Int
)

enum class CardType {
    POINTS, ACTION
}

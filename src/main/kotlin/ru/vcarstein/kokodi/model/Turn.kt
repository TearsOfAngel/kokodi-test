package ru.vcarstein.kokodi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
data class Turn(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JsonIgnore
    val gameSession: GameSession,

    @ManyToOne
    @JsonIgnore
    val player: Player,

    val cardName: String,
    val cardValue: Int,
    val cardType: CardType,

    val description: String
)

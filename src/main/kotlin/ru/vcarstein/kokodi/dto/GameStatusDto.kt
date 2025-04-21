package ru.vcarstein.kokodi.dto

import ru.vcarstein.kokodi.model.GameStatus

data class GameStatusDto(
    val gameId: Long,
    val status: GameStatus,
    val deckSize: Int,
    val players: List<PlayerStatus>,
    val currentTurn: String?
)

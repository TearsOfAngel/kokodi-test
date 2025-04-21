package ru.vcarstein.kokodi.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
data class GameSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Enumerated(EnumType.STRING)
    var status: GameStatus = GameStatus.WAITING,

    @OneToMany(mappedBy = "gameSession")
    @JsonManagedReference
    val players: MutableList<Player> = mutableListOf(),

    @ElementCollection
    val deck: MutableList<Card> = mutableListOf(),

    @OneToMany(mappedBy = "gameSession")
    val turns: MutableList<Turn> = mutableListOf(),

    var currentPlayerIndex: Int = 0
) {
    override fun toString(): String {
        return "GameSession(id=$id, status=$status, currentPlayerIndex=$currentPlayerIndex, players=${players.map { it.id }})"
    }
}

enum class GameStatus {
    WAITING, IN_PROGRESS, FINISHED
}

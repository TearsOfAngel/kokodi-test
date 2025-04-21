package ru.vcarstein.kokodi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vcarstein.kokodi.dto.GameStatusDto
import ru.vcarstein.kokodi.dto.PlayerStatus
import ru.vcarstein.kokodi.exception.GameFinishedException
import ru.vcarstein.kokodi.exception.GameNotFoundException
import ru.vcarstein.kokodi.exception.NotPlayerTurnException
import ru.vcarstein.kokodi.model.*
import ru.vcarstein.kokodi.repository.GameSessionRepository
import ru.vcarstein.kokodi.repository.PlayerRepository
import ru.vcarstein.kokodi.repository.TurnRepository

@Service
class GameService(private val gameRepo: GameSessionRepository,
                  private val playerRepo: PlayerRepository,
                  private val turnRepo: TurnRepository
) {
    companion object {
        private const val WINNING_SCORE = 30
    }

    @Transactional
    fun createGame(appUser: AppUser): GameSession {
        val gameUser = playerRepo.findById(appUser.gamer.id).orElseThrow()

        val gameSession = GameSession()
        gameSession.players.add(gameUser)
        gameUser.gameSession = gameSession

        val savedSession = gameRepo.save(gameSession)
        playerRepo.save(gameUser)

        return savedSession
    }

    fun joinGame(gameId: Long, appUser: AppUser): GameSession {
        val game = gameRepo.findById(gameId).orElseThrow { GameNotFoundException(gameId) }
        if (game.status != GameStatus.WAITING) throw IllegalStateException("Game already started")
        if (game.players.size >= 4) throw IllegalStateException("Game is full")

        val gameUser = playerRepo.findById(appUser.gamer.id).orElseThrow()
        gameUser.gameSession = game
        game.players.add(gameUser)

        playerRepo.save(gameUser)
        return gameRepo.save(game)
    }

    fun startGame(gameId: Long, appUser: AppUser): GameSession {
        val game = gameRepo.findById(gameId).orElseThrow { GameNotFoundException(gameId) }
        if (game.status != GameStatus.WAITING) throw IllegalStateException("Game already started")
        if (game.players.size < 2) throw IllegalStateException("Not enough players to start")

        game.status = GameStatus.IN_PROGRESS
        game.deck.addAll(generateDeck().shuffled())
        return gameRepo.save(game)
    }

    @Transactional
    fun makeTurn(gameId: Long, playerId: Long): Turn {
        val game = gameRepo.findById(gameId).orElseThrow { GameNotFoundException(gameId) }
        if (game.status != GameStatus.IN_PROGRESS) throw GameFinishedException()

        val currentPlayer = game.players[game.currentPlayerIndex]
        if (currentPlayer.id != playerId) throw NotPlayerTurnException()

        if (currentPlayer.isBlocked) {
            currentPlayer.isBlocked = false
            val skippedTurn = Turn(
                gameSession = game,
                player = currentPlayer,
                cardName = "Skip",
                cardValue = 0,
                cardType = CardType.ACTION,
                description = "${currentPlayer.name} пропустил ход"
            )
            nextTurn(game)
            gameRepo.save(game)
            return turnRepo.save(skippedTurn)
        }

        if (game.deck.isEmpty()) throw IllegalStateException("Deck is empty")

        val card = game.deck.removeFirst()
        var description = ""

        when (card.name) {
            "Block" -> {
                val next = getNextPlayerIndex(game)
                game.players[next].isBlocked = true
                description = "${currentPlayer.name} сыграл Block: ${game.players[next].name} пропустит ход"
            }

            "Steal" -> {
                val target = game.players
                    .firstOrNull { it.id != currentPlayer.id && it.score > 0 }

                if (target != null) {
                    val stolen = minOf(card.value, target.score)
                    target.score -= stolen
                    currentPlayer.score += stolen
                    description = "${currentPlayer.name} украл $stolen очков у ${target.name}"
                } else {
                    description = "${currentPlayer.name} сыграл Steal, но ни у кого нет очков для кражи"
                }
            }

            "DoubleDown" -> {
                currentPlayer.score = minOf(currentPlayer.score * 2, WINNING_SCORE)
                description = "${currentPlayer.name} удвоил очки до ${currentPlayer.score}"
            }

            else -> {
                currentPlayer.score += card.value
                description = "${currentPlayer.name} получил ${card.value} очков (${card.name})"
            }
        }

        val turn = Turn(
            gameSession = game,
            player = currentPlayer,
            cardName = card.name,
            cardValue = card.value,
            cardType = card.type,
            description = description
        )

        if (currentPlayer.score >= WINNING_SCORE) {
            game.status = GameStatus.FINISHED
            description += " — Победа!"
            game.players.forEach { it.score = 0 }
        } else {
            nextTurn(game)
        }

        gameRepo.save(game)
        return turnRepo.save(turn)
    }

    fun getGameStatus(gameId: Long): GameStatusDto {
        val game = gameRepo.findById(gameId).orElseThrow { GameNotFoundException(gameId) }
        val players = game.players.map {
            PlayerStatus(
                id = it.id,
                name = it.name,
                score = it.score,
                isBlocked = it.isBlocked
            )
        }

        val currentPlayer = if (game.status == GameStatus.IN_PROGRESS) {
            game.players[game.currentPlayerIndex].name
        } else null

        return GameStatusDto(
            gameId = game.id,
            status = game.status,
            deckSize = game.deck.size,
            players = players,
            currentTurn = currentPlayer
        )
    }

    private fun nextTurn(game: GameSession) {
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % game.players.size
    }

    private fun getNextPlayerIndex(game: GameSession): Int =
        (game.currentPlayerIndex + 1) % game.players.size

    private fun generateDeck(): List<Card> {
        val cardPool = listOf(
            Card("2 Points", CardType.POINTS, 2),
            Card("3 Points", CardType.POINTS, 3),
            Card("4 Points", CardType.POINTS, 4),
            Card("5 Points", CardType.POINTS, 5),
            Card("6 Points", CardType.POINTS, 6),
            Card("7 Points", CardType.POINTS, 7),
            Card("Block", CardType.ACTION, 1),
            Card("Steal", CardType.ACTION, 3),
            Card("Steal", CardType.ACTION, 5),
            Card("DoubleDown", CardType.ACTION, 2),
        )

        return List(50) { cardPool.random() }
    }
}
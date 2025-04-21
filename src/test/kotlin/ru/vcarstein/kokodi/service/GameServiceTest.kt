package ru.vcarstein.kokodi.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import ru.vcarstein.kokodi.repository.GameSessionRepository
import ru.vcarstein.kokodi.repository.PlayerRepository
import ru.vcarstein.kokodi.repository.TurnRepository
import ru.vcarstein.kokodi.model.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class GameServiceTest {

    @Mock
    lateinit var gameRepo: GameSessionRepository

    @Mock
    lateinit var playerRepo: PlayerRepository

    @Mock
    lateinit var turnRepo: TurnRepository

    @InjectMocks
    lateinit var gameService: GameService

    @Test
    fun `should create game and add player to the game session`() {
        val gameUser = Player(id = 1, name = "Player One", score = 10)
        val appUser = AppUser(
            id = 1,
            gamer = gameUser,
            password = "password",
            username = "username",
            name = "name")
        val gameSession = GameSession()
        gameSession.players.add(gameUser)
        gameUser.gameSession = gameSession

        `when`(playerRepo.findById(gameUser.id)).thenReturn(Optional.of(gameUser))
        `when`(gameRepo.save(any())).thenReturn(gameSession)
        `when`(playerRepo.save(any())).thenReturn(gameUser)

        val createdGameSession = gameService.createGame(appUser)

        assertNotNull(createdGameSession)
        assertEquals(1, createdGameSession.players.size)
        assertTrue(createdGameSession.players.contains(gameUser))
        verify(gameRepo).save(any())
        verify(playerRepo).save(gameUser)
    }

    @Test
    fun `should create game and allow player to join`() {
        val creator = Player(id = 1, name = "Creator", score = 0)
        val joiner = Player(id = 2, name = "Joiner", score = 0)
        val appCreator = AppUser(
            id = 1,
            username = "creatorUser",
            password = "pass",
            name = "CreatorName",
            role = Role.USER,
            gamer = creator
        )
        val appJoiner = AppUser(
            id = 2,
            username = "joinerUser",
            password = "pass",
            name = "JoinerName",
            role = Role.USER,
            gamer = joiner
        )
        val gameSession = GameSession(status = GameStatus.WAITING)
        gameSession.id = 100
        gameSession.players.add(creator)

        `when`(playerRepo.findById(creator.id)).thenReturn(Optional.of(creator))
        `when`(playerRepo.findById(joiner.id)).thenReturn(Optional.of(joiner))
        `when`(gameRepo.findById(any())).thenReturn(Optional.of(gameSession))
        `when`(gameRepo.save(any())).thenAnswer { it.arguments[0] }
        `when`(playerRepo.save(any())).thenAnswer { it.arguments[0] }

        val createdSession = gameService.createGame(appCreator)
        val updatedSession = gameService.joinGame(createdSession.id, appJoiner)

        assertEquals(2, updatedSession.players.size)
        assertTrue(updatedSession.players.any { it.name == "Creator" })
        assertTrue(updatedSession.players.any { it.name == "Joiner" })
    }

    @Test
    fun `should steal points from another player`() {
        val thief = Player(id = 1, name = "Thief", score = 5)
        val victim = Player(id = 2, name = "Victim", score = 10)
        val game = GameSession(
            id = 42,
            status = GameStatus.IN_PROGRESS,
            currentPlayerIndex = 0,
            players = mutableListOf(thief, victim),
            deck = mutableListOf(Card("Steal", CardType.ACTION, 3))
        )
        thief.gameSession = game
        victim.gameSession = game

        `when`(gameRepo.findById(game.id)).thenReturn(Optional.of(game))
        `when`(turnRepo.save(any())).thenAnswer { it.arguments[0] }
        `when`(gameRepo.save(any())).thenAnswer { it.arguments[0] }

        val turn = gameService.makeTurn(game.id, thief.id)

        assertEquals(8, thief.score) // 5 + 3
        assertEquals(7, victim.score) // 10 - 3
        assertTrue(turn.description.contains("украл 3 очков у"))
    }
}
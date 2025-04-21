package ru.vcarstein.kokodi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import ru.vcarstein.kokodi.auth.JwtService
import ru.vcarstein.kokodi.model.AppUser
import ru.vcarstein.kokodi.model.GameSession
import ru.vcarstein.kokodi.model.Player
import ru.vcarstein.kokodi.model.Role
import ru.vcarstein.kokodi.repository.AppUserRepository

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var appUserRepository: AppUserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var token1: String
    lateinit var token2: String
    lateinit var user1: AppUser
    lateinit var user2: AppUser

    /**
     * Данный тест симулирует игру
     */
    @Test
    @Transactional
    fun `full game simulation until someone wins`() {
        initializeUsersData()

        // 1. Игрок 1 создаёт игру
        val gameJson = mockMvc.perform(
            post("/games")
                .header("Authorization", "Bearer $token1")
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val game = objectMapper.readValue(gameJson, GameSession::class.java)

        // 2. Игрок 2 подключается
        mockMvc.perform(
            post("/games/${game.id}/join")
                .header("Authorization", "Bearer $token2")
        )
            .andExpect(status().isOk)

        // 3. Игрок 1 стартует игру
        mockMvc.perform(
            post("/games/${game.id}/start")
                .header("Authorization", "Bearer $token1")
        )
            .andExpect(status().isOk)

        while (true) {
            val statusJson = mockMvc.perform(
                get("/games/${game.id}/status")
                    .header("Authorization", "Bearer $token1")
            )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

            val statusNode = objectMapper.readTree(statusJson)
            val status = statusNode.get("status").asText()
            val currentTurn = statusNode.get("currentTurn")?.asText()

            println("Статус игры: $status. Ходит: $currentTurn")

            if (status == "FINISHED") {
                println("Игра окончена!")
                break
            }

            val currentToken = when (currentTurn) {
                user1.gamer.name -> token1
                user2.gamer.name -> token2
                else -> error("Неизвестный игрок: $currentTurn")
            }

            // Выполняем ход текущего игрока
            val turnResponse = mockMvc.perform(
                post("/games/${game.id}/turn")
                    .header("Authorization", "Bearer $currentToken")
            )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

            val turn = objectMapper.readTree(turnResponse)
            println("Ход: ${turn.get("description").asText()}")
        }

        // Финальная проверка: кто-то набрал 30 очков
        val finalStatus = mockMvc.perform(
            get("/games/${game.id}/status")
                .header("Authorization", "Bearer $token1")
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val statusNode = objectMapper.readTree(finalStatus)
        val status = statusNode.get("status").asText()

        assert(status == "FINISHED") { "Игра не завершена!" }
    }

    private fun initializeUsersData() {
        val gamer1 = Player(name = "Player1")
        user1 = AppUser(
            username = "user1",
            password = passwordEncoder.encode("pass1"),
            name = "User One",
            gamer = gamer1,
            role = Role.USER
        )
        user1 = appUserRepository.save(user1)
        token1 = jwtService.generateToken(user1)

        val gamer2 = Player(name = "Player2")
        user2 = AppUser(
            username = "user2",
            password = passwordEncoder.encode("pass2"),
            name = "User Two",
            gamer = gamer2,
            role = Role.USER
        )
        user2 = appUserRepository.save(user2)
        token2 = jwtService.generateToken(user2)
    }
}
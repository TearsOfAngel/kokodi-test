package ru.vcarstein.kokodi.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.vcarstein.kokodi.dto.GameStatusDto
import ru.vcarstein.kokodi.model.AppUser
import ru.vcarstein.kokodi.model.GameSession
import ru.vcarstein.kokodi.model.Turn
import ru.vcarstein.kokodi.service.GameService

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: GameService
) {
    @PostMapping
    fun createGame(@AuthenticationPrincipal user: AppUser): GameSession = gameService.createGame(user)

    @PostMapping("/{id}/join")
    fun joinGame(@PathVariable id: Long, @AuthenticationPrincipal user: AppUser): GameSession = gameService.joinGame(id, user)

    @PostMapping("/{id}/start")
    fun startGame(@PathVariable id: Long, @AuthenticationPrincipal user: AppUser): GameSession = gameService.startGame(id, user)

    @PostMapping("/{id}/turn")
    fun makeTurn(@PathVariable id: Long, @AuthenticationPrincipal user: AppUser): Turn =
        gameService.makeTurn(id, user.gamer.id)

    @GetMapping("/{id}/status")
    fun getStatus(@PathVariable id: Long): GameStatusDto = gameService.getGameStatus(id)
}
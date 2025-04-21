package ru.vcarstein.kokodi.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException::class)
    fun handleGameNotFound(ex: GameNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse("Игра не найдена", "ID: ${ex.gameId}")
        )

    @ExceptionHandler(NotPlayerTurnException::class)
    fun handleNotPlayerTurn(ex: NotPlayerTurnException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse("Сейчас не ваш ход")
        )

    @ExceptionHandler(GameFinishedException::class)
    fun handleGameFinished(ex: GameFinishedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse("Игра уже завершена")
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse("Невозможно выполнить действие", ex.message)
        )

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse("Внутренняя ошибка сервера", ex.message)
        )
}
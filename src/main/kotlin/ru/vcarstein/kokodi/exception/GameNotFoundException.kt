package ru.vcarstein.kokodi.exception

class GameNotFoundException(val gameId: Long) : RuntimeException("Game with id $gameId not found")
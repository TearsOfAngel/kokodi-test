package ru.vcarstein.kokodi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.vcarstein.kokodi.model.GameSession

@Repository
interface GameSessionRepository: JpaRepository<GameSession, Long> {
}
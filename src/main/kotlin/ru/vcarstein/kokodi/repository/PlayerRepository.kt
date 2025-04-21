package ru.vcarstein.kokodi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.vcarstein.kokodi.model.Player

@Repository
interface PlayerRepository: JpaRepository<Player, Long> {
}
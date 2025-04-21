package ru.vcarstein.kokodi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.vcarstein.kokodi.model.AppUser

@Repository
interface AppUserRepository: JpaRepository<AppUser, Long> {

    fun findByUsername(username: String): AppUser?
}
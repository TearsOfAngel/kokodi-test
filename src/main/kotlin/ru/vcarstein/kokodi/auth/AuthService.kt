package ru.vcarstein.kokodi.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.vcarstein.kokodi.dto.AuthRequest
import ru.vcarstein.kokodi.dto.AuthResponse
import ru.vcarstein.kokodi.dto.RegisterRequest
import ru.vcarstein.kokodi.model.AppUser
import ru.vcarstein.kokodi.model.Player
import ru.vcarstein.kokodi.repository.AppUserRepository

@Service
class AuthService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun register(request: RegisterRequest): AuthResponse {
        val user = AppUser(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            gamer = Player(
                name = request.username
            )
        )
        appUserRepository.save(user)
        val token = jwtService.generateToken(user)
        return AuthResponse(token)
    }

    fun authenticate(request: AuthRequest): AuthResponse {
        val user = appUserRepository.findByUsername(request.username)
            ?: throw RuntimeException("User not found")
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw RuntimeException("Invalid credentials")
        }
        return AuthResponse(jwtService.generateToken(user))
    }
}
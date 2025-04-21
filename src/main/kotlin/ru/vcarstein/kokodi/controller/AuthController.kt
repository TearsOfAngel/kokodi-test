package ru.vcarstein.kokodi.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.vcarstein.kokodi.auth.AuthService
import ru.vcarstein.kokodi.dto.AuthRequest
import ru.vcarstein.kokodi.dto.AuthResponse
import ru.vcarstein.kokodi.dto.RegisterRequest

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): AuthResponse =
        authService.register(request)

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): AuthResponse =
        authService.authenticate(request)
}
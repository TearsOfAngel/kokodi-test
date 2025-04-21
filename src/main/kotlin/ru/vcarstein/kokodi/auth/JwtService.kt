package ru.vcarstein.kokodi.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.vcarstein.kokodi.model.AppUser
import java.util.*


@Service
class JwtService(
    @Value("\${jwt.expiration}") val expirationTime: Long,
    @Value("\${jwt.secret}") val secret: String,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(user: AppUser): String {
        return Jwts.builder()
            .setSubject(user.username)
            .claim("role", user.role.name)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime * 1000))
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject
}
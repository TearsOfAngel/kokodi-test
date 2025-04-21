package ru.vcarstein.kokodi.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Table(name = "player")
@Entity
data class Player(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val name: String,

    var score: Int = 0,

    var isBlocked: Boolean = false,

    @ManyToOne
    @JsonBackReference
    var gameSession: GameSession? = null,

    @OneToOne(mappedBy = "gamer", cascade = [CascadeType.PERSIST])
    @JsonIgnore
    val appUser: AppUser? = null
)

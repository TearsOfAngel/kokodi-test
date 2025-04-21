package ru.vcarstein.kokodi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
data class AppUser(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    private val username: String,

    @JsonIgnore
    private val password: String,

    val name: String,

    @OneToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "gamer_id")
    val gamer: Player,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER

) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(role)
    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
    override fun toString(): String {
        return "AppUser(id=$id, username=$username, name=$name, role=$role)"
    }
}

enum class Role : GrantedAuthority {
    USER;
    override fun getAuthority(): String = name
}
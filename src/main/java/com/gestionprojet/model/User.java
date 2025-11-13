package com.gestionprojet.model;

import com.gestionprojet.utils.SHA256;
import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column
    private String role;
    @Column
    private String email;
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Project> projects;

    public void setPasswordHash(String password) {
        this.passwordHash = SHA256.sha256(password);
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
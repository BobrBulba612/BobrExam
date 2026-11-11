package com.bobr.exam.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String passwordHash;

    private String examType;
    private Integer grade;
    private String subjects;

    private Integer xp = 0;
    private Integer streak = 0;
    private Integer hearts = 5;
}

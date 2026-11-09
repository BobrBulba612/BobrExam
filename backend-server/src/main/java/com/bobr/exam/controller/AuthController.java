package com.bobr.exam.controller;

import com.bobr.exam.dto.AuthDTO;
import com.bobr.exam.model.User;
import com.bobr.exam.repository.UserRepository;
import com.bobr.exam.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO.RegisterRequest req) throws Exception {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname());
        // Простое хеширование (в проде использовать BCrypt)
        user.setPasswordHash(hashPassword(req.getPassword()));
        user.setXp(0);
        user.setStreak(0);
        user.setHearts(5);
        userRepository.save(user);
        
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest req) throws Exception {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getPasswordHash().equals(hashPassword(req.getPassword()))) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}

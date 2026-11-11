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

    public static boolean strongPassword(String p) {
        if (p == null || p.length() < 8) return false;
        boolean low = false, up = false, dig = false;
        for (char c : p.toCharArray()) {
            if (Character.isDigit(c)) dig = true;
            else if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
        }
        return low && up && dig;
    }

    public static String hash(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] h = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(h);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO.RegisterRequest req) throws Exception {
        if (req.getEmail() == null || !req.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email");
        }
        if (req.getNickname() == null || req.getNickname().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nickname required");
        }
        if (!strongPassword(req.getPassword())) {
            return ResponseEntity.badRequest()
                    .body("Password needs 8+ chars, a lowercase, an UPPERCASE letter and a digit");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            return ResponseEntity.badRequest().body("Nickname already taken");
        }
        User user = new User();
        user.setEmail(req.getEmail().trim());
        user.setNickname(req.getNickname().trim());
        user.setPasswordHash(hash(req.getPassword()));
        user.setLanguage(req.getLanguage() == null ? "ru" : req.getLanguage());
        user.setXp(0);
        user.setStreak(0);
        user.setHearts(5);
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest req) throws Exception {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (user == null || !user.getPasswordHash().equals(hash(req.getPassword()))) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }
}
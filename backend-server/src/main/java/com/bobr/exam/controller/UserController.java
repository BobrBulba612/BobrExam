package com.bobr.exam.controller;

import com.bobr.exam.dto.TaskDTO;
import com.bobr.exam.model.User;
import com.bobr.exam.repository.UserProgressRepository;
import com.bobr.exam.repository.UserRepository;
import com.bobr.exam.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository users;
    private final UserProgressRepository progress;
    private final JwtService jwt;
    private final ObjectMapper om = new ObjectMapper();

    public UserController(UserRepository users, UserProgressRepository progress, JwtService jwt) {
        this.users = users;
        this.progress = progress;
        this.jwt = jwt;
    }

    private User current(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String t = auth.substring(7);
        if (!jwt.isTokenValid(t)) return null;
        return users.findByEmail(jwt.extractEmail(t)).orElse(null);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setup(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody TaskDTO.SetupRequest req) throws Exception {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        if (req.getGrade() == null || req.getGrade() < 5 || req.getGrade() > 11) {
            return ResponseEntity.badRequest().body("Grade must be 5..11");
        }
        if (req.getSubjects() == null || req.getSubjects().isEmpty()
                || req.getSubjects().size() > 5) {
            return ResponseEntity.badRequest().body("Select 1..5 subjects");
        }
        u.setGrade(req.getGrade());
        u.setExamType(req.getGrade() == 9 ? "ОГЭ"
                : req.getGrade() >= 10 ? "ЕГЭ" : "ШКОЛА");
        u.setSubjects(om.writeValueAsString(req.getSubjects()));
        users.save(u);
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody TaskDTO.ProfileUpdateRequest req) {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        if (req.getNickname() != null && !req.getNickname().trim().isEmpty()
                && !req.getNickname().equals(u.getNickname())) {
            if (users.existsByNickname(req.getNickname())) {
                return ResponseEntity.badRequest().body("Nickname taken");
            }
            u.setNickname(req.getNickname().trim());
        }
        if (req.getEmail() != null && req.getEmail().contains("@")
                && !req.getEmail().equals(u.getEmail())) {
            if (users.existsByEmail(req.getEmail())) {
                return ResponseEntity.badRequest().body("Email taken");
            }
            u.setEmail(req.getEmail().trim());
        }
        if (req.getAvatar() != null) u.setAvatar(req.getAvatar());
        if (req.getLanguage() != null) u.setLanguage(req.getLanguage());
        users.save(u);
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody TaskDTO.PasswordChangeRequest req) throws Exception {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        if (!u.getPasswordHash().equals(AuthController.hash(req.getOldPassword()))) {
            return ResponseEntity.status(400).body("Old password wrong");
        }
        if (!AuthController.strongPassword(req.getNewPassword())) {
            return ResponseEntity.badRequest()
                    .body("Password needs 8+ chars, lowercase, UPPERCASE and digit");
        }
        u.setPasswordHash(AuthController.hash(req.getNewPassword()));
        users.save(u);
        return ResponseEntity.ok("Password changed");
    }

    @DeleteMapping
    public ResponseEntity<?> delete(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        progress.deleteByUserId(u.getId());
        users.delete(u);
        return ResponseEntity.ok("Account deleted");
    }
}
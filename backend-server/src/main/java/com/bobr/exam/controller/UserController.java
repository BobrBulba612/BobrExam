package com.bobr.exam.controller;

import com.bobr.exam.dto.TaskDTO;
import com.bobr.exam.model.User;
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
    private final JwtService jwt;
    private final ObjectMapper om = new ObjectMapper();

    public UserController(UserRepository users, JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    private User current(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String t = auth.substring(7);
        if (!jwt.isTokenValid(t)) return null;
        return users.findByEmail(jwt.extractEmail(t)).orElse(null);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestHeader(value = "Authorization", required = false) String auth,
                                   @RequestBody TaskDTO.SetupRequest req) throws Exception {
        User u = current(auth);
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");
        if (req.getSubjects() == null || req.getSubjects().isEmpty() || req.getSubjects().size() > 4) {
            return ResponseEntity.badRequest().body("Select 1..4 subjects");
        }
        u.setExamType(req.getExamType());
        u.setSubjects(om.writeValueAsString(req.getSubjects()));
        users.save(u);
        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }
}

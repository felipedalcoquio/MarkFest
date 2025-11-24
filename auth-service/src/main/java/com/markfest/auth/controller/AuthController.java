package com.markfest.auth.controller;

import com.markfest.auth.model.User;
import com.markfest.auth.repository.UserRepository;
import com.markfest.auth.security.JwtProvider;
import com.markfest.auth.dto.LoginRequest;
import com.markfest.auth.dto.RegisterRequest;
import com.markfest.auth.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    String email = request.getEmail();
    String password = request.getPassword();
    String name = request.getName();

    if (userRepository.findByEmail(email).isPresent()) {
        return ResponseEntity.badRequest().body(Map.of("error", "email taken"));
    }

    User u = new User();
    u.setEmail(email);
    u.setName(name);
    u.setPassword(passwordEncoder.encode(password));
    userRepository.save(u);

    return ResponseEntity.ok(Map.of("message", "registered"));
}


        @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid"));
        }

        User u = userOpt.get();
        if (!passwordEncoder.matches(password, u.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid"));
        }

        String token = jwtProvider.generateToken(u.getEmail());
        AuthResponse response = new AuthResponse(token);
        return ResponseEntity.ok(response);
    }
}


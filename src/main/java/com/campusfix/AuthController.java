
package com.campusfix;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody AppUser user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Email already registered");
        }

        if (user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().length() < 8) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Name, email, and password of at least 8 characters are required");
        }

        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // New registrations can only create student accounts.
        user.setRole("STUDENT");

        userRepository.save(user);

        return "Registration successful";
    }

    @PostMapping("/login")
    public java.util.Map<String, String> loginUser(
            @RequestBody AppUser loginData) {

        AppUser user = userRepository.findByEmail(loginData.getEmail())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!passwordEncoder.matches(
                loginData.getPassword(), user.getPassword())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return java.util.Map.of(
                "message", "Login successful",
                "name", user.getName(),
                "role", user.getRole(),
                "token", token);

    }

}

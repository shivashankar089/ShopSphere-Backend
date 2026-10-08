package com.shopsphere.backend.auth;

import com.shopsphere.backend.security.JwtService;
import com.shopsphere.backend.user.Role;
import com.shopsphere.backend.user.User;
import com.shopsphere.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            String phone,
            String country,
            String city,
            String pincode,
            String role
    ) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(
            String token,
            Long id,
            String name,
            String email,
            String role,
            String country,
            String city,
            String phone,
            String pincode,
            boolean approved
    ) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            return error(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        Role role = "SELLER".equalsIgnoreCase(req.role()) ? Role.SELLER : Role.CUSTOMER;

        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(role);
        u.setCountry(req.country() != null && !req.country().isBlank() ? req.country() : "India");
        u.setCity(req.city() != null && !req.city().isBlank() ? req.city() : "Hyderabad");
        u.setPhone(req.phone() != null ? req.phone() : "");
        u.setPincode(req.pincode() != null && !req.pincode().isBlank() ? req.pincode() : "500081");
        u.setApproved(role != Role.SELLER);
        users.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User u = users.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        if (u == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            return error(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }
        if (!u.isActive()) {
            return error(HttpStatus.FORBIDDEN, "This account has been disabled");
        }
        return ResponseEntity.ok(toResponse(u));
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(
                jwt.generate(u),
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name(),
                u.getCountry() != null ? u.getCountry() : "India",
                u.getCity() != null ? u.getCity() : "Hyderabad",
                u.getPhone(),
                u.getPincode() != null ? u.getPincode() : "500081",
                u.isApproved()
        );
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}

package ru.servicecenter.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.servicecenter.server.dto.auth.AuthResponse;
import ru.servicecenter.server.dto.auth.LoginRequest;
import ru.servicecenter.server.security.CustomUserDetails;
import ru.servicecenter.server.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getUser().getRole().getName().name();
        String token = jwtService.generateToken(userDetails, role);

        return AuthResponse.builder()
                .userId(userDetails.getUser().getId())
                .token(token)
                .username(userDetails.getUsername())
                .fullName(userDetails.getUser().getFullName())
                .role(userDetails.getUser().getRole().getName())
                .build();
    }
}

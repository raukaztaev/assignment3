package com.example.mvp.service;

import com.example.mvp.dto.auth.LoginRequest;
import com.example.mvp.dto.auth.LoginResponse;
import com.example.mvp.entity.UserEntity;
import com.example.mvp.exception.UnauthorizedException;
import com.example.mvp.repository.UserRepository;
import com.example.mvp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        UserEntity user = userRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        String token = jwtService.generateToken(userDetails);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), user.getUsername(), user.getRole().name());
    }
}

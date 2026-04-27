package com.example.mvp.service;

import com.example.mvp.dto.auth.LoginRequest;
import com.example.mvp.dto.auth.LoginResponse;
import com.example.mvp.entity.UserEntity;
import com.example.mvp.exception.TooManyRequestsException;
import com.example.mvp.exception.UnauthorizedException;
import com.example.mvp.repository.UserRepository;
import com.example.mvp.security.JwtService;
import com.example.mvp.security.LoginAttemptService;
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
    private final LoginAttemptService loginAttemptService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       UserRepository userRepository,
                       JwtService jwtService,
                       LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    public LoginResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.username())) {
            throw new TooManyRequestsException("Too many login attempts. Try again later");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            loginAttemptService.registerFailedAttempt(request.username());
            throw new UnauthorizedException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        UserEntity user = userRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        loginAttemptService.registerSuccessfulLogin(request.username());
        String token = jwtService.generateToken(userDetails);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), user.getUsername(), user.getRole().name());
    }
}

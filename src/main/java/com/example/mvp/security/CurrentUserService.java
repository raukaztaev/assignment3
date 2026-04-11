package com.example.mvp.security;

import com.example.mvp.entity.UserEntity;
import com.example.mvp.exception.NotFoundException;
import com.example.mvp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new NotFoundException("User not found");
        }
        return userRepository.findByUsernameAndActiveTrue(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

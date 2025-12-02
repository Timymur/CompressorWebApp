package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.repositories.UserRepository;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User GetCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        return findByLogin(login);

    }


    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public void registerUser(User user) {

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

}

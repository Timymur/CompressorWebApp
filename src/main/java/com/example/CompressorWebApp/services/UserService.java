package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.repositories.UserRepository;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

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

    public List<User> findByStationId(Long id) {
        return userRepository.findByStationId(id);
    }

    public void closeShift(User user) {
        user.setInWork(false);
        userRepository.save(user);
    }

    public void openShift(User user) {
        user.setInWork(true);
        userRepository.save(user);
    }

    public Optional<User> findCurrentShiftUserByStationId(Long stationId) {
        List<User> workers = userRepository.findByStationId(stationId);
        return workers.stream()
                .filter(User::isInWork)
                .findFirst();
    }

    public void save(User user)  {
        userRepository.save(user);
    }

    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }
}

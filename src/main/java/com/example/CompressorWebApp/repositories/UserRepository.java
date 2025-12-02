package com.example.CompressorWebApp.repositories;


import com.example.CompressorWebApp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRole(String role);


    User findByLogin(String login);
    List<User> findByStation_Id(Long stationId);

}


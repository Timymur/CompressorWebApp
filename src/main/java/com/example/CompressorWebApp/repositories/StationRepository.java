package com.example.CompressorWebApp.repositories;

import com.example.CompressorWebApp.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByCity(String city);
}


package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.repositories.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public Optional<Station> findById(Long id) {
        return stationRepository.findById(id);
    }

}

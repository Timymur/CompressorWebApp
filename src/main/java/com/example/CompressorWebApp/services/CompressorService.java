package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.repositories.CompressorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompressorService {

    private final CompressorRepository compressorRepository;

    public CompressorService(CompressorRepository compressorRepository){
        this.compressorRepository = compressorRepository;
    }

    public List<Compressor> findByStationId(Long id) {
        return compressorRepository.findByStationId(id);
    }
}

package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.repositories.CompressorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CompressorService {

    private final CompressorRepository compressorRepository;

    public CompressorService(CompressorRepository compressorRepository){
        this.compressorRepository = compressorRepository;
    }

    public Optional<Compressor> findById(Long id){
        return compressorRepository.findById(id);
    }

    public List<Compressor> findByStationId(Long id) {
        return compressorRepository.findByStationId(id);
    }

    public void save(Compressor compressor) {
        compressorRepository.save(compressor);
    }

    @Transactional
    public void changeState(Long id, CompressorState state) {
        Compressor compressor = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компрессор с id=" + id + " не найден"));
        compressor.setState(state);
        compressorRepository.save(compressor);
    }

    @Transactional
    public void updateWorkHours(Long id, double newHours) {
        Compressor compressor = findById(id).orElseThrow();
        compressor.setWorkHours(newHours);
        compressorRepository.save(compressor);
    }

}

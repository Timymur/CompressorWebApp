package com.example.CompressorWebApp.repositories;

import com.example.CompressorWebApp.models.Compressor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompressorRepository extends JpaRepository<Compressor, Long> {



    List<Compressor> findByStationId(Long id);
}
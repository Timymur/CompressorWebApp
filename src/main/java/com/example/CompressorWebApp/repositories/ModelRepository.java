package com.example.CompressorWebApp.repositories;



import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<CompressorModel, Long> {

    List<CompressorModel> findAll();

    boolean existsByModelName(String modelName);

    Optional<CompressorModel> findByModelName(String modelName);

}

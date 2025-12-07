package com.example.CompressorWebApp.repositories;



import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelRepository extends JpaRepository<CompressorModel, Long> {

    List<CompressorModel> findAll();


}

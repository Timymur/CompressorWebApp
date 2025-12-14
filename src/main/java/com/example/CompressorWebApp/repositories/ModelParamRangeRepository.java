package com.example.CompressorWebApp.repositories;

import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.ModelParamRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelParamRangeRepository extends JpaRepository<ModelParamRange, Long> {

    List<ModelParamRange> findByCompressorModel_Id(Long compressorModelId);


}

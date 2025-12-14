package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.repositories.ModelParamRangeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelParamRangeService {

    private final ModelParamRangeRepository modelParamRangeRepository;

    public ModelParamRangeService(ModelParamRangeRepository modelParamRangeRepository){
        this.modelParamRangeRepository = modelParamRangeRepository;
    }

    public List<ModelParamRange>  findByCompressorModelId(Long id){
        List<ModelParamRange> ranges =
                modelParamRangeRepository.findByCompressorModel_Id(id);
        return ranges;
    }

    public Optional<ModelParamRange> findById(Long id){
        Optional<ModelParamRange> modelParamRange = modelParamRangeRepository.findById(id);
        return modelParamRange;
    }

    public void save(ModelParamRange range){
        modelParamRangeRepository.save(range);
    }

}

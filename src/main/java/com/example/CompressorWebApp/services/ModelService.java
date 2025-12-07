package com.example.CompressorWebApp.services;



import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.repositories.ModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository){
        this.modelRepository = modelRepository;
    }

    public List<CompressorModel> findAll(){
        return modelRepository.findAll();
    }

    public Optional<CompressorModel> findById(Long modelId){
        return modelRepository.findById(modelId);
    }

}

package com.example.CompressorWebApp.services;


import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.repositories.ParameterRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;

    public ParameterService(ParameterRepository parameterRepository){
        this.parameterRepository = parameterRepository;
    }

    public boolean existsByParameterName(String parameterName){
        return parameterRepository.existsByParameterName(parameterName);
    }

    public Optional<Parameter> findByParameterName(String parameterName){
        return parameterRepository.findByParameterName(parameterName);
    }
    public void save(Parameter parameter) {
        parameterRepository.save(parameter);
    }
}

package com.example.CompressorWebApp.services;



import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.repositories.ModelParamRangeRepository;
import com.example.CompressorWebApp.repositories.ModelRepository;
import com.example.CompressorWebApp.repositories.ParameterRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private  final ParameterRepository parameterRepository;
    private  final ModelParamRangeRepository modelParamRangeRepository;

    public ModelService(ModelRepository modelRepository , ParameterRepository parameterRepository, ModelParamRangeRepository modelParamRangeRepository){
        this.modelRepository = modelRepository;
        this.modelParamRangeRepository = modelParamRangeRepository;
        this.parameterRepository = parameterRepository;
    }


    public List<CompressorModel> findAll(){
        return modelRepository.findAll();
    }

    public Optional<CompressorModel> findById(Long modelId){
        return modelRepository.findById(modelId);
    }

    public boolean existsByModelName(String modelName){
        return modelRepository.existsByModelName(modelName);
    }
    public Optional<CompressorModel> findByModelName(String modelName){
        return modelRepository.findByModelName(modelName);
    }
    @Transactional
    public CompressorModel createModelWithDefaultRanges(String modelName) {
        CompressorModel model = new CompressorModel(modelName);
        model = modelRepository.save(model);

        List<Parameter> params = parameterRepository.findAll();
        for (Parameter p : params) {
            ModelParamRange range =
                    new ModelParamRange(model, p, 0.0, 0.0);
            modelParamRangeRepository.save(range);
        }

        return model;
    }




}

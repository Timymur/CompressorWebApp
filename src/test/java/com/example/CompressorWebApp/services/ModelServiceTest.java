package com.example.CompressorWebApp.services;

import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.repositories.ModelParamRangeRepository;
import com.example.CompressorWebApp.repositories.ModelRepository;
import com.example.CompressorWebApp.repositories.ParameterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ParameterRepository parameterRepository;

    @Mock
    private ModelParamRangeRepository modelParamRangeRepository;

    @InjectMocks
    private ModelService modelService;


    @Test
    void findAll_shouldReturnListFromRepository() {
        List<CompressorModel> mockModels = List.of(new CompressorModel(), new CompressorModel());
        when(modelRepository.findAll()).thenReturn(mockModels);

        List<CompressorModel> result = modelService.findAll();

        assertThat(result).hasSize(2);
        verify(modelRepository).findAll();
    }

    @Test
    void findById_shouldReturnOptionalFromRepository() {
        Long id = 1L;
        CompressorModel model = new CompressorModel();
        when(modelRepository.findById(id)).thenReturn(Optional.of(model));

        Optional<CompressorModel> result = modelService.findById(id);

        assertThat(result).isPresent();
        verify(modelRepository).findById(id);
    }

    @Test
    void existsByModelName_shouldReturnBooleanFromRepository() {
        String name = "Model X";
        when(modelRepository.existsByModelName(name)).thenReturn(true);

        boolean exists = modelService.existsByModelName(name);

        assertThat(exists).isTrue();
        verify(modelRepository).existsByModelName(name);
    }

    @Test
    void findByModelName_shouldReturnOptionalFromRepository() {
        String name = "Model Y";
        CompressorModel model = new CompressorModel();
        when(modelRepository.findByModelName(name)).thenReturn(Optional.of(model));

        Optional<CompressorModel> result = modelService.findByModelName(name);

        assertThat(result).isPresent();
        verify(modelRepository).findByModelName(name);
    }


    @Test
    void createModelWithDefaultRanges_shouldSaveModel_thenCreateRangesForAllParameters() {

        String modelName = "TestModel";
        CompressorModel savedModel = new CompressorModel(modelName);
        savedModel.setId(100L);

        List<Parameter> allParams = List.of(
                new Parameter("Pressure", "bar"),
                new Parameter( "Temperature", "C"),
                new Parameter("gasPollution", "%")
        );

        when(modelRepository.save(any(CompressorModel.class))).thenReturn(savedModel);
        when(parameterRepository.findAll()).thenReturn(allParams);


        CompressorModel result = modelService.createModelWithDefaultRanges(modelName);


        ArgumentCaptor<CompressorModel> modelCaptor = ArgumentCaptor.forClass(CompressorModel.class);
        verify(modelRepository).save(modelCaptor.capture());
        CompressorModel capturedModel = modelCaptor.getValue();
        assertThat(capturedModel.getModelName()).isEqualTo(modelName);


        verify(parameterRepository).findAll();
        verify(modelParamRangeRepository, times(allParams.size())).save(any(ModelParamRange.class));


        ArgumentCaptor<ModelParamRange> rangeCaptor = ArgumentCaptor.forClass(ModelParamRange.class);
        verify(modelParamRangeRepository, times(allParams.size())).save(rangeCaptor.capture());

        List<ModelParamRange> savedRanges = rangeCaptor.getAllValues();
        assertThat(savedRanges).hasSize(allParams.size());

        for (int i = 0; i < allParams.size(); i++) {
            ModelParamRange range = savedRanges.get(i);
            Parameter expectedParam = allParams.get(i);
            assertThat(range.getCompressorModel()).isEqualTo(savedModel);
            assertThat(range.getParameter()).isEqualTo(expectedParam);
            assertThat(range.getMinValue()).isEqualTo(0.0);
            assertThat(range.getMaxValue()).isEqualTo(0.0);
        }


        assertThat(result).isSameAs(savedModel);
    }

    @Test
    void createModelWithDefaultRanges_shouldWorkEvenWhenNoParametersExist() {

        String modelName = "EmptyModel";
        CompressorModel savedModel = new CompressorModel(modelName);
        when(modelRepository.save(any(CompressorModel.class))).thenReturn(savedModel);
        when(parameterRepository.findAll()).thenReturn(List.of());


        CompressorModel result = modelService.createModelWithDefaultRanges(modelName);


        verify(modelRepository).save(any(CompressorModel.class));
        verify(parameterRepository).findAll();
        verify(modelParamRangeRepository, never()).save(any());
        assertThat(result).isSameAs(savedModel);
    }
}
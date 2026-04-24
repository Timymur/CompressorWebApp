package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.services.ModelParamRangeService;
import com.example.CompressorWebApp.services.ModelService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModelParamRangeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModelParamRangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;

    @MockitoBean
    private ModelParamRangeService modelParamRangeService;

    private CompressorModel testModel;
    private ModelParamRange testRange;

    @BeforeEach
    void setUp() {
        testModel = new CompressorModel();
        testModel.setId(1L);
        testModel.setModelName("Test Model");

        Parameter param = new Parameter();
        param.setId(10L);
        param.setParameterName("Давление");

        testRange = new ModelParamRange();
        testRange.setId(100L);
        testRange.setCompressorModel(testModel);
        testRange.setParameter(param);
        testRange.setMinValue(0.0);
        testRange.setMaxValue(100.0);
    }



    @Test
    @DisplayName("GET /selectModel - отображение списка моделей")
    @WithMockUser
    void selectModel_Success() throws Exception {
        when(modelService.findAll()).thenReturn(List.of(testModel));

        mockMvc.perform(get("/selectModel"))
                .andExpect(status().isOk())
                .andExpect(view().name("selectModel"))
                .andExpect(model().attribute("models", List.of(testModel)));

        verify(modelService).findAll();
    }


    @Test
    @DisplayName("GET /selectParam - успешное отображение параметров модели")
    @WithMockUser
    void selectParamGet_Success() throws Exception {
        when(modelParamRangeService.findByCompressorModelId(1L)).thenReturn(List.of(testRange));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));

        mockMvc.perform(get("/selectParam").param("modelId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("selectParam"))
                .andExpect(model().attribute("ranges", List.of(testRange)))
                .andExpect(model().attribute("model", testModel));

        verify(modelParamRangeService).findByCompressorModelId(1L);
        verify(modelService).findById(1L);
    }

    @Test
    @DisplayName("GET /selectParam - модель не найдена")
    @WithMockUser
    void selectParamGet_ModelNotFound() {
        when(modelParamRangeService.findByCompressorModelId(99L)).thenReturn(List.of());
        when(modelService.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/selectParam").param("modelId", "99"));
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Такой модели нет", exception.getCause().getMessage());
    }



    @Test
    @DisplayName("POST /selectParam - успешное отображение параметров модели")
    @WithMockUser
    void selectParamPost_Success() throws Exception {
        when(modelParamRangeService.findByCompressorModelId(1L)).thenReturn(List.of(testRange));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));

        mockMvc.perform(post("/selectParam").param("modelId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("selectParam"))
                .andExpect(model().attribute("ranges", List.of(testRange)))
                .andExpect(model().attribute("model", testModel));
    }



    @Test
    @DisplayName("GET /ranges/{id}/edit - диапазон не найден")
    @WithMockUser
    void showEditForm_NotFound() {
        when(modelParamRangeService.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/ranges/999/edit"));
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Не найдено диапазона параметров", exception.getCause().getMessage());
    }





    @Test
    @DisplayName("POST /editModelParam - успешное обновление диапазона")
    @WithMockUser
    void editParam_Success() throws Exception {
        when(modelParamRangeService.findById(100L)).thenReturn(Optional.of(testRange));
        when(modelParamRangeService.findByCompressorModelId(1L)).thenReturn(List.of(testRange));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));

        mockMvc.perform(post("/editModelParam")
                        .param("modelParamId", "100")
                        .param("minValue", "10.5")
                        .param("maxValue", "90.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/selectParam?modelId=1"));


        verify(modelParamRangeService).save(testRange);
        assertEquals(10.5, testRange.getMinValue());
        assertEquals(90.5, testRange.getMaxValue());
    }

    @Test
    @DisplayName("POST /editModelParam - диапазон не найден")
    @WithMockUser
    void editParam_RangeNotFound() {
        when(modelParamRangeService.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/editModelParam")
                    .param("modelParamId", "999")
                    .param("minValue", "0")
                    .param("maxValue", "100"));
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Такой  связи модели и параметра нет", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("POST /editModelParam - minValue > maxValue -> редирект на форму редактирования")
    @WithMockUser
    void editParam_MinGreaterThanMax_RedirectToEdit() throws Exception {
        when(modelParamRangeService.findById(100L)).thenReturn(Optional.of(testRange));


        mockMvc.perform(post("/editModelParam")
                        .param("modelParamId", "100")
                        .param("minValue", "150")
                        .param("maxValue", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ranges/100/edit"));


        verify(modelParamRangeService, never()).save(any());
    }

    @Test
    @DisplayName("POST /editModelParam - модель не найдена после обновления")
    @WithMockUser
    void editParam_ModelNotFoundAfterUpdate() {
        when(modelParamRangeService.findById(100L)).thenReturn(Optional.of(testRange));
        when(modelService.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/editModelParam")
                    .param("modelParamId", "100")
                    .param("minValue", "10")
                    .param("maxValue", "90"));
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Такой модели нет", exception.getCause().getMessage());
        verify(modelParamRangeService).save(testRange);
    }
}
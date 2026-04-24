package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.services.ModelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;

    @Test
    @DisplayName("GET /addModel - возвращает страницу добавления модели")
    void addModelPage() throws Exception {
        mockMvc.perform(get("/addModel"))
                .andExpect(status().isOk())
                .andExpect(view().name("addModel"));
    }

    @Test
    @DisplayName("POST /addModel - успешное создание новой модели")
    void addModelPost_Success() throws Exception {
        String newModelName = "NewModel";
        when(modelService.existsByModelName(newModelName)).thenReturn(false);

        mockMvc.perform(post("/addModel")
                        .param("modelName", newModelName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/addParameter"));

        verify(modelService).existsByModelName(newModelName);
        verify(modelService).createModelWithDefaultRanges(newModelName);
    }

    @Test
    @DisplayName("POST /addModel - модель с таким именем уже существует")
    void addModelPost_ModelExists() throws Exception {
        String existingModelName = "ExistingModel";
        when(modelService.existsByModelName(existingModelName)).thenReturn(true);

        mockMvc.perform(post("/addModel")
                        .param("modelName", existingModelName))
                .andExpect(status().isOk())
                .andExpect(view().name("addModel"))
                .andExpect(model().attribute("errorMessage", "Такая модель уже существует"));

        verify(modelService).existsByModelName(existingModelName);
        verify(modelService, never()).createModelWithDefaultRanges(anyString());
    }
}
package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.services.ModelService;
import com.example.CompressorWebApp.services.ParameterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParameterController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParameterService parameterService;

    @MockitoBean
    private ModelService modelService;

    @Test
    @DisplayName("GET /addParameter - возвращает форму добавления параметра")
    @WithMockUser
    void addParameter_ReturnsForm() throws Exception {
        mockMvc.perform(get("/addParameter"))
                .andExpect(status().isOk())
                .andExpect(view().name("addParameter"));
    }

    @Test
    @DisplayName("POST /addParameter - успешное добавление нового параметра")
    @WithMockUser
    void addParameterPost_Success() throws Exception {
        when(parameterService.existsByParameterName("Давление")).thenReturn(false);

        mockMvc.perform(post("/addParameter")
                        .param("parameterName", "Давление")
                        .param("unitOfMeasurement", "бар"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/addParameter"));

        verify(parameterService).save(any(Parameter.class));
    }

    @Test
    @DisplayName("POST /addParameter - параметр уже существует, возврат на форму с ошибкой")
    @WithMockUser
    void addParameterPost_Duplicate() throws Exception {
        when(parameterService.existsByParameterName("Давление")).thenReturn(true);

        mockMvc.perform(post("/addParameter")
                        .param("parameterName", "Давление")
                        .param("unitOfMeasurement", "бар"))
                .andExpect(status().isOk())
                .andExpect(view().name("addParameter"))
                .andExpect(model().attribute("errorMessage", "Такой параметр уже существует"));

        verify(parameterService, never()).save(any());
    }
}
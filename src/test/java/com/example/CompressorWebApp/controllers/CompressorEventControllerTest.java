package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.*;
import com.example.CompressorWebApp.services.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompressorEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompressorEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CompressorService compressorService;
    @MockitoBean
    private ModelParamRangeService modelParamRangeService;
    @MockitoBean
    private CompressorEventService compressorEventService;

    private Compressor testCompressor;
    private CompressorEvent testEvent;

    @BeforeEach
    void setUp() {
        Station testStation = new Station();
        testStation.setId(1L);

        CompressorModel testModel = new CompressorModel();
        testModel.setId(1L);
        testModel.setModelName("Model A");

        testCompressor = new Compressor(123, 0, testModel, testStation);
        testCompressor.setId(1L);


        Parameter param = new Parameter();
        param.setId(10L);
        param.setParameterName("Давление");

        testEvent = new CompressorEvent();
        testEvent.setId(100L);
        testEvent.setText("Test event");
        testEvent.setCompressor(testCompressor);
        testEvent.setParameter(param);
        testEvent.setDate(LocalDate.now());
    }

    @Test
    @DisplayName("GET /openAccidentLog/{id} - успешное отображение журнала аварий")
    @WithMockUser
    void openAccidentLog_Success() throws Exception {
        when(compressorService.findById(1L)).thenReturn(Optional.of(testCompressor));
        when(compressorEventService.findLast10ByCompressorId(1L)).thenReturn(List.of(testEvent));

        mockMvc.perform(get("/openAccidentLog/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("accidentLog"))
                .andExpect(model().attributeExists("compressor"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("compressor", testCompressor))
                .andExpect(model().attribute("events", List.of(testEvent)));

        verify(compressorService).findById(1L);
        verify(compressorEventService).findLast10ByCompressorId(1L);
    }


}
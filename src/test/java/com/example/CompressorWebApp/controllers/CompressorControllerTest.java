package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.*;
import com.example.CompressorWebApp.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompressorController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompressorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;
    @MockitoBean
    private StationService stationService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CompressorService compressorService;
    @MockitoBean
    private ModelParamRangeService modelParamRangeService;

    private Compressor testCompressor;
    private CompressorModel testModel;
    private Station testStation;
    private User testUser;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        @Primary
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers("/addCompressor").hasAnyRole("MANAGER", "ADMIN")
                            .anyRequest().authenticated()
                    )
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(1L);

        testModel = new CompressorModel();
        testModel.setId(1L);
        testModel.setModelName("Model A");

        testCompressor = new Compressor(123, 0, testModel, testStation);
        testCompressor.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("manager");
        testUser.setRole("manager");
        testUser.setStation(testStation);
    }

    @Test
    @DisplayName("GET /compressor/{id} - успешное отображение страницы компрессора")
    @WithMockUser
    void compressorPage_Success() throws Exception {
        when(compressorService.findById(1L)).thenReturn(Optional.of(testCompressor));
        when(modelParamRangeService.findByCompressorModelId(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/compressor/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("compressor"))
                .andExpect(model().attributeExists("compressor"))
                .andExpect(model().attributeExists("allRanges"))
                .andExpect(model().attributeExists("oilRanges"))
                .andExpect(model().attributeExists("coolantRanges"))
                .andExpect(model().attributeExists("gasRangesByStep"))
                .andExpect(model().attributeExists("bkuRanges"));

        verify(compressorService).findById(1L);
        verify(modelParamRangeService).findByCompressorModelId(testModel.getId());
    }



    @Test
    @DisplayName("GET /addCompressor - доступ для MANAGER")
    @WithMockUser(roles = "MANAGER")
    void addCompressorPage_Manager() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testUser);
        when(modelService.findAll()).thenReturn(List.of(testModel));

        mockMvc.perform(get("/addCompressor"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCompressor"))
                .andExpect(model().attributeExists("models"))
                .andExpect(model().attribute("stationId", 1L))
                .andExpect(model().attributeDoesNotExist("stations"));

        verify(userService).GetCurrentUser();
        verify(modelService).findAll();
    }

    @Test
    @DisplayName("GET /addCompressor - доступ для ADMIN")
    @WithMockUser(roles = "ADMIN")
    void addCompressorPage_Admin() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        when(userService.GetCurrentUser()).thenReturn(admin);
        when(modelService.findAll()).thenReturn(List.of(testModel));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/addCompressor"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCompressor"))
                .andExpect(model().attributeExists("models"))
                .andExpect(model().attributeExists("stations"))
                .andExpect(model().attributeDoesNotExist("stationId"));

        verify(stationService).findAll();
    }



    @Test
    @DisplayName("POST /addCompressor - успешное добавление компрессора менеджером")
    @WithMockUser(roles = "MANAGER")
    void addCompressorPost_Success_Manager() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testUser);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));
        when(compressorService.findByStationId(1L)).thenReturn(List.of());

        mockMvc.perform(post("/addCompressor")
                        .with(csrf())
                        .param("modelId", "1")
                        .param("serialNumber", "555")
                        .param("stationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(compressorService).save(any(Compressor.class));
    }

    @Test
    @DisplayName("POST /addCompressor - менеджер пытается добавить на чужую станцию")
    @WithMockUser(roles = "MANAGER")
    void addCompressorPost_WrongStation_Manager() throws Exception {
        Station otherStation = new Station();
        otherStation.setId(2L);
        when(userService.GetCurrentUser()).thenReturn(testUser);
        when(stationService.findById(2L)).thenReturn(Optional.of(otherStation));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));
        when(modelService.findAll()).thenReturn(List.of(testModel));

        mockMvc.perform(post("/addCompressor")
                        .with(csrf())
                        .param("modelId", "1")
                        .param("serialNumber", "555")
                        .param("stationId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCompressor"))
                .andExpect(model().attribute("errorMessage", "Неверно указан id станции"));

        verify(compressorService, never()).save(any());
    }

    @Test
    @DisplayName("POST /addCompressor - невалидный серийный номер (не число)")
    @WithMockUser(roles = "ADMIN")
    void addCompressorPost_InvalidSerialNumber() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        when(userService.GetCurrentUser()).thenReturn(admin);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));
        when(modelService.findAll()).thenReturn(List.of(testModel));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/addCompressor")
                        .with(csrf())
                        .param("modelId", "1")
                        .param("serialNumber", "abc")
                        .param("stationId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCompressor"))
                .andExpect(model().attribute("errorMessage", "Введите целое число без дробной части"));

        verify(compressorService, never()).save(any());
    }

    @Test
    @DisplayName("POST /addCompressor - серийный номер уже существует")
    @WithMockUser(roles = "ADMIN")
    void addCompressorPost_DuplicateSerialNumber() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        when(userService.GetCurrentUser()).thenReturn(admin);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(modelService.findById(1L)).thenReturn(Optional.of(testModel));
        when(compressorService.findByStationId(1L)).thenReturn(List.of(testCompressor));
        when(modelService.findAll()).thenReturn(List.of(testModel));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/addCompressor")
                        .with(csrf())
                        .param("modelId", "1")
                        .param("serialNumber", "123")
                        .param("stationId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCompressor"))
                .andExpect(model().attribute("errorMessage", "Компрессор с таким номером уже существует"));
    }

    @Test
    @DisplayName("POST /compressor/{id}/state - изменение состояния")
    @WithMockUser
    void changeState_Success() throws Exception {
        when(compressorService.findById(1L)).thenReturn(Optional.of(testCompressor));

        mockMvc.perform(post("/compressor/1/state")
                        .with(csrf())
                        .param("value", "WORKING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/compressor/1"));

        verify(compressorService).changeState(1L, CompressorState.WORKING);
    }


}
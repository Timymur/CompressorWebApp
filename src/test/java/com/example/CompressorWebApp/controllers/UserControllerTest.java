package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.servlet.ServletException;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private StationService stationService;

    @MockitoBean
    private CompressorService compressorService;

    private Station testStation;
    private User testUser;
    private Compressor testCompressor;
    private CompressorModel testModel;

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(1L);
        testStation.setCity("CITY");
        testStation.setCodeWord("secret");



        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setLogin("ivanov");
        testUser.setRole("manager");
        testUser.setStation(testStation);
        testUser.setInWork(false);

        testModel = new CompressorModel();
        testModel.setId(10L);
        testModel.setModelName("Test Model");

        testCompressor = new Compressor();
        testCompressor.setId(100L);
        testCompressor.setSerialNumber(123);
        testCompressor.setCompressorModel(testModel);
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }


    @Test
    @DisplayName("GET /registration - отображение формы с списком станций")
    @WithMockUser
    void registrationPage() throws Exception {
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("stations", List.of(testStation)));
    }


    @Test
    @DisplayName("GET /auth - отображение страницы аутентификации")
    @WithMockUser
    void authPage() throws Exception {
        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }


    @Test
    @DisplayName("POST /auth - возврат страницы auth")
    @WithMockUser
    void authPost() throws Exception {
        mockMvc.perform(post("/auth"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }


    @Test
    @DisplayName("POST /registration - успешная регистрация")
    @WithMockUser
    void registrationSuccess() throws Exception {
        when(userService.findByLogin("ivanov")).thenReturn(null);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));

        mockMvc.perform(post("/registration")
                        .param("firstName", "Иван")
                        .param("secondName", "Иванов")
                        .param("login", "Ivanov")
                        .param("password", "pass")
                        .param("confirmPassword", "pass")
                        .param("stationId", "1")
                        .param("codeword", "secret")
                        .param("jobTitle", "Начальник"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authorization"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    @DisplayName("POST /registration - логин уже занят")
    @WithMockUser
    void registrationLoginExists() throws Exception {
        when(userService.findByLogin("ivanov")).thenReturn(testUser);
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/registration")
                        .param("firstName", "Петр")
                        .param("secondName", "Петров")
                        .param("login", "Ivanov")
                        .param("password", "pass")
                        .param("confirmPassword", "pass")
                        .param("stationId", "1")
                        .param("codeword", "secret")
                        .param("jobTitle", "Машинист"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("errorMessage", "Пользователь с таким логином уже существует"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /registration - неверное кодовое слово")
    @WithMockUser
    void registrationWrongCodeWord() throws Exception {
        when(userService.findByLogin(anyString())).thenReturn(null);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/registration")
                        .param("firstName", "Иван")
                        .param("secondName", "Иванов")
                        .param("login", "ivan")
                        .param("password", "pass")
                        .param("confirmPassword", "pass")
                        .param("stationId", "1")
                        .param("codeword", "wrong")
                        .param("jobTitle", "Машинист"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("errorMessage", "Неверное кодовое слово"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /registration - пароли не совпадают")
    @WithMockUser
    void registrationPasswordMismatch() throws Exception {
        when(userService.findByLogin(anyString())).thenReturn(null);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/registration")
                        .param("firstName", "Иван")
                        .param("secondName", "Иванов")
                        .param("login", "ivan")
                        .param("password", "pass1")
                        .param("confirmPassword", "pass2")
                        .param("stationId", "1")
                        .param("codeword", "secret")
                        .param("jobTitle", "Машинист"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("errorMessage", "Пароли не совпадают"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /registration - неверная должность")
    @WithMockUser
    void registrationInvalidJobTitle() throws Exception {
        when(userService.findByLogin(anyString())).thenReturn(null);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(post("/registration")
                        .param("firstName", "Иван")
                        .param("secondName", "Иванов")
                        .param("login", "ivan")
                        .param("password", "pass")
                        .param("confirmPassword", "pass")
                        .param("stationId", "1")
                        .param("codeword", "secret")
                        .param("jobTitle", "Директор"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("errorMessage", "Неверная должность"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /registration - станция не найдена (исключение)")
    @WithMockUser
    void registrationStationNotFound() {
        when(userService.findByLogin(anyString())).thenReturn(null);
        when(stationService.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/registration")
                    .param("firstName", "Иван")
                    .param("secondName", "Иванов")
                    .param("login", "ivan")
                    .param("password", "pass")
                    .param("confirmPassword", "pass")
                    .param("stationId", "99")
                    .param("codeword", "secret")
                    .param("jobTitle", "Машинист"));
        });


        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalArgumentException, "Expected IllegalArgumentException but got " + cause.getClass());
        assertEquals("Станция не найдена", cause.getMessage());
    }


    @Test
    @DisplayName("GET /openCloseShift - пользователь не аутентифицирован -> auth")
    @WithMockUser
    void openCloseShiftUserNull() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/openCloseShift"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    @DisplayName("GET /openCloseShift - у пользователя нет станции -> home с сообщением")
    @WithMockUser
    void openCloseShiftNullStation() throws Exception {
        testUser.setStation(null);
        when(userService.GetCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/openCloseShift"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"))
                .andExpect(model().attribute("nullStation", "Вы не относитесь ни к одной станции"));
    }




    @Test
    @DisplayName("GET /openCloseShift - нет компрессоров на станции")
    @WithMockUser
    void openCloseShift_NoCompressors() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testUser);
        when(compressorService.findByStationId(1L)).thenReturn(null);
        when(userService.findByStationId(1L)).thenReturn(List.of(testUser));

        mockMvc.perform(get("/openCloseShift"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("nullCompressors", "На этой станции нет компрессоров"));
    }



}
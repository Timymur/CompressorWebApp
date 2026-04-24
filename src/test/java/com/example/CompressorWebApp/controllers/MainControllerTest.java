package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@AutoConfigureMockMvc(addFilters = false)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompressorService compressorService;

    @MockitoBean
    private StationService stationService;

    private User testAdmin;
    private User testManager;
    private Station testStation;
    private Compressor testCompressor;
    private CompressorModel testModel;

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(1L);
        testStation.setCity("City");

        testAdmin = new User();
        testAdmin.setId(1L);
        testAdmin.setRole("admin");

        testManager = new User();
        testManager.setId(2L);
        testManager.setRole("manager");
        testManager.setStation(testStation);
        testManager.setInWork(false);

        testModel = new CompressorModel();
        testModel.setId(10L);
        testModel.setModelName("Test Model");

        testCompressor = new Compressor();
        testCompressor.setId(100L);
        testCompressor.setSerialNumber(123);
        testCompressor.setCompressorModel(testModel);
    }



    @Test
    @DisplayName("GET / - пользователь не аутентифицирован (null) -> редирект на auth")
    void home_UserNull_RedirectToAuth() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"))
                .andExpect(model().attributeDoesNotExist("user"));
    }

    @Test
    @DisplayName("GET / - админ без выбранной станции в сессии -> редирект на select-station")
    void home_Admin_NoStationSelected_Redirect() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testAdmin);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-station"));
    }

    @Test
    @DisplayName("GET / - админ с выбранной станцией -> отображение main")
    void home_Admin_WithSelectedStation_Success() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testAdmin);
        when(stationService.findById(1L)).thenReturn(Optional.of(testStation));
        when(userService.findByStationId(1L)).thenReturn(List.of(testManager));
        when(compressorService.findByStationId(1L)).thenReturn(List.of(testCompressor));

        mockMvc.perform(get("/").sessionAttr("selectedStationId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("user", testAdmin))
                .andExpect(model().attribute("station", testStation))
                .andExpect(model().attributeExists("compressors"));

        verify(stationService).findById(1L);
        verify(userService).findByStationId(1L);
        verify(compressorService).findByStationId(1L);
    }


    @Test
    @DisplayName("GET / - менеджер без привязанной станции -> отображение main с сообщением")
    void home_Manager_NullStation_ShowMessage() throws Exception {
        testManager.setStation(null);
        when(userService.GetCurrentUser()).thenReturn(testManager);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"))
                .andExpect(model().attribute("nullStation", "Вы не относитесь ни к одной станции"));
    }

    @Test
    @DisplayName("GET / - менеджер с корректной станцией -> отображение main")
    void home_Manager_Success() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testManager);
        when(userService.findByStationId(1L)).thenReturn(List.of(testManager));
        when(compressorService.findByStationId(1L)).thenReturn(List.of(testCompressor));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("user", testManager))
                .andExpect(model().attribute("station", testStation))
                .andExpect(model().attributeExists("workers"))
                .andExpect(model().attributeExists("compressors"));
    }



    @Test
    @DisplayName("GET / - есть работник на смене -> onShiftUser установлен")
    void home_WorkerOnShift() throws Exception {
        User onShift = new User();
        onShift.setId(3L);
        onShift.setInWork(true);
        when(userService.GetCurrentUser()).thenReturn(testManager);
        when(userService.findByStationId(1L)).thenReturn(List.of(testManager, onShift));
        when(compressorService.findByStationId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("onShiftUser", onShift));
    }

    @Test
    @DisplayName("GET /select-station - пользователь null -> auth")
    void selectStation_UserNull() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(null);
        mockMvc.perform(get("/select-station"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    @DisplayName("GET /select-station - не админ -> редирект на /")
    void selectStation_NotAdmin() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testManager);
        mockMvc.perform(get("/select-station"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("GET /select-station - админ -> список станций")
    void selectStation_Admin() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testAdmin);
        when(stationService.findAll()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/select-station"))
                .andExpect(status().isOk())
                .andExpect(view().name("select-station"))
                .andExpect(model().attribute("stations", List.of(testStation)));
    }



    @Test
    @DisplayName("POST /select-station - админ выбирает станцию -> сохраняет в сессию и редирект")
    void selectStationPost_Admin_Success() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testAdmin);

        mockMvc.perform(post("/select-station")
                        .param("stationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("selectedStationId", 1L));
    }

    @Test
    @DisplayName("POST /select-station - не админ -> редирект на /")
    void selectStationPost_NotAdmin() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(testManager);

        mockMvc.perform(post("/select-station")
                        .param("stationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist("selectedStationId"));
    }

    @Test
    @DisplayName("POST /select-station - пользователь null -> auth")
    void selectStationPost_UserNull() throws Exception {
        when(userService.GetCurrentUser()).thenReturn(null);

        mockMvc.perform(post("/select-station")
                        .param("stationId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }
}
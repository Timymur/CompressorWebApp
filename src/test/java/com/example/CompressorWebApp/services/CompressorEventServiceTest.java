package com.example.CompressorWebApp.services;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorEvent;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.repositories.CompressorEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompressorEventServiceTest {

    @Mock
    private CompressorEventRepository compressorEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompressorEventService compressorEventService;


    @Test
    void findByCompressorId_shouldReturnListFromRepository() {

        Long compressorId = 1L;
        List<CompressorEvent> mockEvents = List.of(new CompressorEvent(), new CompressorEvent());
        when(compressorEventRepository.findByCompressorId(compressorId)).thenReturn(mockEvents);


        List<CompressorEvent> result = compressorEventService.findByCompressorId(compressorId);


        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockEvents);
        verify(compressorEventRepository, times(1)).findByCompressorId(compressorId);
    }



    @Test
    void createEvent_shouldCreateAndSaveEvent_whenUserPresent() {

        Compressor compressor = new Compressor();
        Station station = new Station();
        station.setId(100L);
        compressor.setStation(station);

        Parameter parameter = new Parameter("oil_pressure", "Bar");
        double value = 150.0;
        double min = 80.0;
        double max = 120.0;

        User mockUser = new User();
        mockUser.setId(42L);
        when(userService.findCurrentShiftUserByStationId(100L)).thenReturn(Optional.of(mockUser));


        compressorEventService.createEvent(compressor, parameter, value, min, max);


        ArgumentCaptor<CompressorEvent> eventCaptor = ArgumentCaptor.forClass(CompressorEvent.class);
        verify(compressorEventRepository).save(eventCaptor.capture());

        CompressorEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getCompressor()).isEqualTo(compressor);
        assertThat(savedEvent.getParameter()).isEqualTo(parameter);
        assertThat(savedEvent.getDate()).isEqualTo(LocalDate.now());
        assertThat(savedEvent.getTime()).isNotNull();
        assertThat(savedEvent.getUser()).isEqualTo(mockUser);
        assertThat(savedEvent.getText()).isEqualTo("Аварийное значение 150,00\nДопустимо: 80,00 - 120,00");
    }



    @Test
    void createEvent_shouldSetCurrentDateAndTime() {

        Compressor compressor = new Compressor();
        Station station = new Station();
        station.setId(1L);
        compressor.setStation(station);
        when(userService.findCurrentShiftUserByStationId(1L)).thenReturn(Optional.empty());


        compressorEventService.createEvent(compressor, new Parameter("gas-1", "Bar"), 99.9, 0.0, 100.0);


        ArgumentCaptor<CompressorEvent> captor = ArgumentCaptor.forClass(CompressorEvent.class);
        verify(compressorEventRepository).save(captor.capture());

        CompressorEvent event = captor.getValue();
        assertThat(event.getDate()).isEqualTo(LocalDate.now());

        assertThat(event.getTime()).isBetween(LocalTime.MIN, LocalTime.MAX);
    }
}
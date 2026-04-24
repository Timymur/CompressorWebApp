package com.example.CompressorWebApp.services;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.repositories.CompressorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompressorServiceTest {

    @Mock
    private CompressorRepository compressorRepository;

    @InjectMocks
    private CompressorService compressorService;


    @Test
    void findById_shouldReturnCompressor_whenExists() {
        Long id = 1L;
        Compressor mockCompressor = new Compressor();
        mockCompressor.setId(id);
        when(compressorRepository.findById(id)).thenReturn(Optional.of(mockCompressor));


        Optional<Compressor> result = compressorService.findById(id);


        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(compressorRepository, times(1)).findById(id);
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenNotExists() {

        Long id = 999L;
        when(compressorRepository.findById(id)).thenReturn(Optional.empty());


        Optional<Compressor> result = compressorService.findById(id);


        assertThat(result).isEmpty();
        verify(compressorRepository, times(1)).findById(id);
    }


    @Test
    void findByStationId_shouldReturnListOfCompressors() {

        Long stationId = 10L;
        List<Compressor> mockList = List.of(new Compressor(), new Compressor());
        when(compressorRepository.findByStationId(stationId)).thenReturn(mockList);


        List<Compressor> result = compressorService.findByStationId(stationId);


        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockList);
        verify(compressorRepository, times(1)).findByStationId(stationId);
    }

    @Test
    void findByStationId_shouldReturnEmptyList_whenNoCompressors() {

        Long stationId = 99L;
        when(compressorRepository.findByStationId(stationId)).thenReturn(List.of());


        List<Compressor> result = compressorService.findByStationId(stationId);


        assertThat(result).isEmpty();
        verify(compressorRepository, times(1)).findByStationId(stationId);
    }


    @Test
    void save_shouldCallRepositorySave() {

        Compressor compressor = new Compressor();
        compressor.setWorkHours(15.56);


        compressorService.save(compressor);


        verify(compressorRepository, times(1)).save(compressor);
    }


    @Test
    void changeState_shouldUpdateState_whenCompressorExists() {

        Long id = 1L;
        CompressorState newState = CompressorState.FALL;
        Compressor compressor = new Compressor();
        compressor.setId(id);
        compressor.setState(CompressorState.WORKING);

        when(compressorRepository.findById(id)).thenReturn(Optional.of(compressor));
        when(compressorRepository.save(any(Compressor.class))).thenReturn(compressor);


        compressorService.changeState(id, newState);


        assertThat(compressor.getState()).isEqualTo(newState);
        verify(compressorRepository).save(compressor);
    }

    @Test
    void changeState_shouldThrowException_whenCompressorNotFound() {

        Long id = 999L;
        when(compressorRepository.findById(id)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> compressorService.changeState(id, CompressorState.WORKING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Компрессор с id=" + id + " не найден");

        verify(compressorRepository, never()).save(any());
    }


    @Test
    void updateWorkHours_shouldUpdateHours_whenCompressorExists() {

        Long id = 5L;
        double newHours = 123.45;
        Compressor existingCompressor = new Compressor();
        existingCompressor.setId(id);
        existingCompressor.setWorkHours(100.0);

        when(compressorRepository.findById(id)).thenReturn(Optional.of(existingCompressor));


        compressorService.updateWorkHours(id, newHours);


        assertThat(existingCompressor.getWorkHours()).isEqualTo(newHours);
        verify(compressorRepository).save(existingCompressor);
    }

    @Test
    void updateWorkHours_shouldThrowException_whenCompressorNotFound() {

        Long id = 999L;
        when(compressorRepository.findById(id)).thenReturn(Optional.empty());



        assertThatThrownBy(() -> compressorService.updateWorkHours(id, 100.0))
                .isInstanceOf(Exception.class);

        verify(compressorRepository, never()).save(any());
    }
}
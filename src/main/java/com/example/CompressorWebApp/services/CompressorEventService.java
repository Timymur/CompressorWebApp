package com.example.CompressorWebApp.services;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorEvent;
import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.repositories.CompressorEventRepository;
import com.example.CompressorWebApp.repositories.CompressorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompressorEventService {

    private final CompressorEventRepository compressorEventRepository;
    private final UserService userService;
    public CompressorEventService(CompressorEventRepository compressorEventRepository, UserService userService){
        this.compressorEventRepository = compressorEventRepository;
        this.userService = userService;
    }

    public List<CompressorEvent> findByCompressorId(Long id) {
        return compressorEventRepository.findByCompressorId(id);
    }

    public List<CompressorEvent> findLast10ByCompressorId(Long id){
        return compressorEventRepository.findLast10ByCompressorId(id, PageRequest.of(0, 10));
    }

    public void createEvent(Compressor compressor, Parameter parameter, double value, double min, double max) {

        CompressorEvent event = new CompressorEvent();
        event.setCompressor(compressor);
        event.setParameter(parameter);
        event.setDate(LocalDate.now());
        event.setTime(LocalTime.now());

        Optional<User> currentShiftUser = userService.findCurrentShiftUserByStationId(compressor.getStation().getId());
        currentShiftUser.ifPresent(event::setUser);

        String text = String.format("Аварийное значение %.2f\nДопустимо: %.2f - %.2f", value, min, max);
        event.setText(text);
        compressorEventRepository.save(event);
    }

}

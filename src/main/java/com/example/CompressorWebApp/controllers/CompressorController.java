package com.example.CompressorWebApp.controllers;


import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.Station;
import com.example.CompressorWebApp.models.User;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.ModelService;
import com.example.CompressorWebApp.services.StationService;
import com.example.CompressorWebApp.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CompressorController {

    private final ModelService modelService;
    private final StationService stationService;
    private  final UserService userService;
    private final CompressorService compressorService;

    public CompressorController(ModelService modelService, StationService stationService, UserService userService,CompressorService compressorService){
        this.modelService = modelService;
        this.stationService = stationService;
        this.userService = userService;
        this.compressorService = compressorService;
    }

    @GetMapping("/compressor")
    public String Compressor(Model model) {
        return "compressor";
    }

    @GetMapping("/addCompressor")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addCompressor(Model model) {

        List<CompressorModel> models = modelService.findAll();
        model.addAttribute("models", models);

        User user = userService.GetCurrentUser();

        if (user.getRole().equals("manager")) {
            Long stationId = user.getStation().getId();
            model.addAttribute("stationId", stationId);

        } else if (user.getRole().equals("admin")) {
            model.addAttribute("stations", stationService.findAll());
        }


        return "addCompressor";
    }


    @PostMapping("/addCompressor")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addCompressorPost(Model model, @RequestParam Long modelId, @RequestParam String serialNumber, @RequestParam Long stationId) {


        Station station = stationService.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        CompressorModel compressorModel = modelService.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Такой модели нет"));

        User user = userService.GetCurrentUser();
        int serialNumberInt;
        try {
            serialNumberInt = Integer.parseInt(serialNumber);

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Введите целое число без дробной части");
            return "addCompressor";
        }

        if (user.getRole().equals("manager")) {
            if (!user.getStation().getId().equals(stationId)){
                model.addAttribute("errorMessage", "Неверно указан id станции");
                return "addCompressor";
            }
        }


        List<Compressor> compressors = compressorService.findByStationId(stationId);

        if (serialNumberInt < 0){
            model.addAttribute("errorMessage", "Номер не может быть меньше нуля");
            return "addCompressor";
        }

        boolean exists = compressors.stream()
                .anyMatch(c -> c.getSerialNumber() == serialNumberInt);

        if (exists) {
            model.addAttribute("errorMessage", "Компрессор с таким номером уже существует");
            return "addCompressor";
        }

        Compressor compressor = new Compressor(serialNumberInt, 0, compressorModel, station);
        compressorService.save(compressor);

        return "redirect:/";
    }
}

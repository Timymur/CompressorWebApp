package com.example.CompressorWebApp.controllers;


import com.example.CompressorWebApp.models.*;
import com.example.CompressorWebApp.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CompressorController {

    private final ModelService modelService;
    private final StationService stationService;
    private  final UserService userService;
    private final CompressorService compressorService;
    private final ModelParamRangeService modelParamRangeService;

    public CompressorController(ModelService modelService, ModelParamRangeService modelParamRangeService, StationService stationService, UserService userService, CompressorService compressorService){
        this.modelService = modelService;
        this.stationService = stationService;
        this.userService = userService;
        this.compressorService = compressorService;
        this.modelParamRangeService = modelParamRangeService;
    }

    @GetMapping("/compressor/{id}")
    public String compressor(Model model, @PathVariable Long id) {

        Compressor compressor = compressorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компрессор не найден"));
        model.addAttribute("compressor", compressor);

        List<ModelParamRange> ranges = modelParamRangeService
                .findByCompressorModelId(compressor.getCompressorModel().getId());


        Map<String, ModelParamRange> allRanges = ranges.stream()
                .collect(Collectors.toMap(
                        r -> r.getParameter().getParameterName(),
                        r -> r,
                        (existing, replacement) -> existing
                ));
        model.addAttribute("allRanges", allRanges);


        List<ModelParamRange> oilRanges = ranges.stream()
                .filter(r -> {
                    String name = r.getParameter().getParameterName();
                    return name.equals("Температура масла") ||
                            name.equals("Давление масла");
                })
                .toList();
        model.addAttribute("oilRanges", oilRanges);


        List<ModelParamRange> coolantRanges = ranges.stream()
                .filter(r -> {
                    String name = r.getParameter().getParameterName();
                    return name.equals("Температура охлаждающей жидкости") ||
                            name.equals("Давление охлаждающей жидкости");
                })
                .toList();
        model.addAttribute("coolantRanges", coolantRanges);


        Map<Integer, List<ModelParamRange>> gasRangesByStep = new HashMap<>();
        for (int step = 1; step <= 4; step++) {
            int s = step;
            List<ModelParamRange> rangesForStep = ranges.stream()
                    .filter(r -> {
                        String name = r.getParameter().getParameterName();
                        return name.equals("Температура газа " + s + "-ой ступени") ||
                                name.equals("Давление газа " + s + "-ой ступени");
                    })
                    .toList();
            gasRangesByStep.put(step, rangesForStep);
        }
        model.addAttribute("gasRangesByStep", gasRangesByStep);


        List<ModelParamRange> bkuRanges = ranges.stream()
                .filter(r -> {
                    String name = r.getParameter().getParameterName();
                    return name.equals("Уровень вибрации") ||
                            name.equals("Процент загазованности") ||
                            name.equals("Индекс влажности газа") ||
                            name.equals("Температура воздуха в компрессорном блоке");
                })
                .toList();
        model.addAttribute("bkuRanges", bkuRanges);

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

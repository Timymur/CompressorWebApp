package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.*;
import com.example.CompressorWebApp.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@Controller
public class CompressorEventController {

    private final ModelService modelService;
    private final UserService userService;
    private final CompressorService compressorService;
    private final ModelParamRangeService modelParamRangeService;
    private final CompressorEventService compressorEventService;

    public CompressorEventController(ModelService modelService, ModelParamRangeService modelParamRangeService, UserService userService, CompressorService compressorService, CompressorEventService compressorEventService){
        this.modelService = modelService;
        this.userService = userService;
        this.compressorService = compressorService;
        this.modelParamRangeService = modelParamRangeService;
        this.compressorEventService = compressorEventService;
    }


    @GetMapping("/openAccidentLog/{id}")
    public String openAccidentLog(@PathVariable("id") Long id, Model model) {

        Compressor compressor = compressorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компрессор не найден"));

        List<CompressorEvent> events = compressorEventService.findLast10ByCompressorId(id);

        model.addAttribute("compressor", compressor);
        model.addAttribute("events", events);
        return "accidentLog";
    }




}

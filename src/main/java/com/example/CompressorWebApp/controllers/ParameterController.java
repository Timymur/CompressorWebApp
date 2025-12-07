package com.example.CompressorWebApp.controllers;


import com.example.CompressorWebApp.models.Parameter;
import com.example.CompressorWebApp.services.ModelService;
import com.example.CompressorWebApp.services.ParameterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ParameterController {


    private final ParameterService parameterService;
    private final ModelService modelService;

    public ParameterController(ParameterService parameterService, ModelService modelService){
        this.parameterService= parameterService;
        this.modelService = modelService;
    }

    @GetMapping("/addParameter")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addParameter(Model model) {


        return "addParameter";
    }

    @PostMapping("/addParameter")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addParameterPost(Model model, @RequestParam String parameterName, @RequestParam String unitOfMeasurement ) {

        if(parameterService.existsByParameterName(parameterName)){
            model.addAttribute("errorMessage", "Такой параметр уже существует");
            return "addParameter";
        }

        Parameter parameter = new Parameter(parameterName, unitOfMeasurement);
        parameterService.save(parameter);

        return "redirect:/addParameter";
    }

}

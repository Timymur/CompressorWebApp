package com.example.CompressorWebApp.controllers;

import com.example.CompressorWebApp.models.CompressorEvent;
import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.services.ModelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService){
        this.modelService = modelService;
    }

    @GetMapping("/addModel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addModel(Model model) {
        return "addModel";
    }


    @PostMapping("/addModel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String addModelPost(Model model, @RequestParam String modelName) {

        if (modelService.existsByModelName(modelName)) {
            model.addAttribute("errorMessage", "Такая модель уже существует");
            return "addModel";
        }

        CompressorModel compressorModel = new CompressorModel(modelName);
        modelService.save(compressorModel);
        return "redirect:/addParameter";

    }

}
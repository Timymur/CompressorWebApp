package com.example.CompressorWebApp.controllers;


import com.example.CompressorWebApp.models.CompressorModel;
import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.ModelParamRangeService;
import com.example.CompressorWebApp.services.ModelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ModelParamRangeController {


    private final ModelService modelService;
    private final ModelParamRangeService modelParamRangeService;

    public ModelParamRangeController(ModelService modelService, ModelParamRangeService modelParamRangeService ){
        this.modelService = modelService;
        this.modelParamRangeService = modelParamRangeService;
    }

    @GetMapping("/selectModel")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String selectModel(Model model) {

        List<CompressorModel> compressorModelList = modelService.findAll();
        model.addAttribute("models", compressorModelList);
        return "selectModel";
    }



    @GetMapping("/selectParam")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String selectParamGet(@RequestParam("modelId") Long id, Model model){

        List<ModelParamRange> ranges = modelParamRangeService.findByCompressorModelId(id);
        model.addAttribute("ranges", ranges);

        CompressorModel compressorModel = modelService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Такой модели нет"));

        model.addAttribute("model", compressorModel);


        return "selectParam";
    }

    @PostMapping("/selectParam")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String selectParam(Model model, @RequestParam Long modelId) {


        List<ModelParamRange> ranges = modelParamRangeService.findByCompressorModelId(modelId);
        model.addAttribute("ranges", ranges);

        CompressorModel compressorModel = modelService.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Такой модели нет"));

        model.addAttribute("model", compressorModel);


        return "selectParam";
    }

    @GetMapping("/ranges/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {

        ModelParamRange modelParamRange = modelParamRangeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Не найдено диапазона параметров"));

        model.addAttribute("range", modelParamRange);
        return "editModel";

    }

    @PostMapping("/editModelParam")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String editParam(Model model, @RequestParam Long modelParamId, @RequestParam Double minValue, @RequestParam Double maxValue ) {


        ModelParamRange range = modelParamRangeService.findById(modelParamId)
                .orElseThrow(() -> new IllegalArgumentException("Такой  связи модели и параметра нет"));


        if(minValue > maxValue){
            model.addAttribute("errorMessage", "Минимальное значение не может быть больше максимального");
            model.addAttribute("range", range);
            return "redirect:/ranges/" + modelParamId + "/edit";
        }
        range.setMinValue(minValue);
        range.setMaxValue(maxValue);
        modelParamRangeService.save(range);

        List<ModelParamRange> ranges = modelParamRangeService.findByCompressorModelId(range.getCompressorModel().getId());
        model.addAttribute("ranges", ranges);

        CompressorModel compressorModel = modelService.findById(range.getCompressorModel().getId())
                .orElseThrow(() -> new IllegalArgumentException("Такой модели нет"));

        model.addAttribute("model", compressorModel);


        Long modelId = range.getCompressorModel().getId();
        return "redirect:/selectParam?modelId=" + modelId;
    }

}
